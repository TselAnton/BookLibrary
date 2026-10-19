package com.tsel.home.project.booklibrary.filter.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.filter.provider.impl.FieldValueProvider;
import com.tsel.home.project.booklibrary.filter.provider.impl.ObservedOptionsProvider;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterOptionCacheTest {

    private final FilterCatalog catalog = new FilterCatalog(TestRow.class);
    private final List<TestRow> rows = List.of(new TestRow("Б", true), new TestRow("А", false), new TestRow("Б", false));

    @BeforeEach
    void resetCounters() {
        CountingValues.reads.set(0);
        CountingValues.caller.set(Thread.currentThread());
        CountingOptions.calls.set(0);
        CountingOptions.worker.set(null);
        CountingOptions.block.set(false);
        CountingOptions.entered = new CountDownLatch(1);
        CountingOptions.release = new CountDownLatch(1);
    }

    @Test
    void testValuesAreCapturedOnceAndDomainsAreBuiltOffTheCallingThread() throws Exception {
        try (FilterOptionCache cache = cache()) {
            cache.replaceRows(rows);
            CompletableFuture<Map<String, List<FilterOption>>> first = cache.options(Map.of());
            Map<String, List<FilterOption>> options = first.get(5, TimeUnit.SECONDS);
            assertEquals(3, CountingValues.reads.get());
            assertNotSame(Thread.currentThread(), CountingOptions.worker.get());
            assertEquals(List.of("А", "Б"), options.get("author").stream().map(FilterOption::label).toList());
            assertEquals(List.of(1L, 2L), options.get("author").stream().map(FilterOption::count).toList());
            assertSame(first, cache.options(Map.of()), "Reopening with the same conditions reuses completed options");
            cache.options(Map.of("read", new ChoiceCondition(Set.of(false)))).get(5, TimeUnit.SECONDS);
            assertEquals(3, CountingValues.reads.get(), "Counting must not reread UI values");
            assertEquals(1, CountingOptions.calls.get(), "Changing a condition must not rebuild option domains");
        }
    }

    @Test
    void testCachedCountsRespectOtherFiltersAndExcludeTheirOwnCondition() throws Exception {
        try (FilterOptionCache cache = cache()) {
            cache.replaceRows(rows);
            Map<String, List<FilterOption>> options = cache.options(Map.of(
                "author", new ChoiceCondition(Set.of("А")), "read", new ChoiceCondition(Set.of(false))
            )).get(5, TimeUnit.SECONDS);
            assertEquals(List.of(1L, 1L), options.get("author").stream().map(FilterOption::count).toList());
            Map<String, List<FilterOption>> read = cache.options(Map.of("read", new ChoiceCondition(Set.of(true)))).get(5, TimeUnit.SECONDS);
            assertEquals(List.of(0L, 1L), read.get("author").stream().map(FilterOption::count).toList());
        }
    }

    @Test
    void testRefreshingUnchangedBooksKeepsTheWarmedIndex() throws Exception {
        try (FilterOptionCache cache = cache()) {
            cache.replaceRows(rows);
            CompletableFuture<Map<String, List<FilterOption>>> first = cache.options(Map.of());
            first.get(5, TimeUnit.SECONDS);
            List<TestRow> sameValues = List.of(new TestRow("Б", true), new TestRow("А", false), new TestRow("Б", false));
            cache.replaceRows(sameValues);
            assertSame(first, cache.options(Map.of()));
            assertEquals(1, CountingOptions.calls.get(), "Viewing unchanged books must not regenerate option domains");
        }
    }

    @Test
    void testCreatingEditingAndDeletingRecordsRebuildsTheIndex() throws Exception {
        try (FilterOptionCache cache = cache()) {
            List<TestRow> current = new ArrayList<>(rows);
            cache.replaceRows(current);
            cache.options(Map.of()).get(5, TimeUnit.SECONDS);
            current.add(new TestRow("В", true));
            cache.replaceRows(current);
            assertEquals(List.of("А", "Б", "В"), labels(cache));
            current.get(0).author = "Г";
            cache.replaceRows(current);
            assertEquals(List.of("А", "Б", "В", "Г"), labels(cache));
            current.remove(1);
            cache.replaceRows(current);
            assertEquals(List.of("Б", "В", "Г"), labels(cache));
        }
    }

    @Test
    void testAnObsoleteBuildCannotReplaceANewerCatalog() throws Exception {
        try (FilterOptionCache cache = cache()) {
            CountingOptions.block.set(true);
            cache.replaceRows(rows);
            CompletableFuture<Map<String, List<FilterOption>>> obsolete = cache.options(Map.of());
            assertTrue(CountingOptions.entered.await(5, TimeUnit.SECONDS));
            assertFalse(obsolete.isDone(), "A blocked background build must not block the calling thread");
            List<TestRow> replacement = List.of(new TestRow("Новый автор", true));
            cache.replaceRows(replacement);
            CompletableFuture<Map<String, List<FilterOption>>> latest = cache.options(Map.of());
            CountingOptions.release.countDown();
            assertEquals(List.of("Новый автор"), latest.get(5, TimeUnit.SECONDS).get("author").stream().map(FilterOption::label).toList());
            assertTrue(obsolete.isCompletedExceptionally());
            assertSame(latest, cache.options(Map.of()));
        }
    }

    @Test
    void testClosingTheCacheRejectsNewWork() {
        FilterOptionCache cache = cache();
        cache.close();
        assertTrue(cache.options(Map.of()).isCompletedExceptionally());
    }

    @Test
    void testCacheMapsAreConfinedToTheCreatingThread() throws Exception {
        try (FilterOptionCache cache = cache()) {
            cache.replaceRows(rows);
            CompletableFuture.runAsync(() -> {
                assertThrows(IllegalStateException.class, () -> cache.options(Map.of()));
                assertThrows(IllegalStateException.class, () -> cache.replaceRows(rows));
                assertThrows(IllegalStateException.class, cache::close);
            }).get(5, TimeUnit.SECONDS);
            assertEquals(List.of("А", "Б"), labels(cache));
        }
    }

    @Test
    void testCacheRetainsRecentlyUsedConditionsAndEvictsTheOldest() throws Exception {
        try (FilterOptionCache cache = cache()) {
            cache.replaceRows(rows);
            CompletableFuture<Map<String, List<FilterOption>>> oldest = cache.options(Map.of());
            oldest.get(5, TimeUnit.SECONDS);
            Map<String, FilterCondition> recent = Map.of(
                "author", new ChoiceCondition(Set.of("А"))
            );
            CompletableFuture<Map<String, List<FilterOption>>> warmed = cache.options(recent);
            warmed.get(5, TimeUnit.SECONDS);
            for (int index = 0; index < 7; index++) {
                assertSame(warmed, cache.options(recent));
                cache.options(Map.of("author", new ChoiceCondition(Set.of("Запрос " + index)))).get(5, TimeUnit.SECONDS);
            }
            assertSame(warmed, cache.options(recent));
            assertNotSame(oldest, cache.options(Map.of()), "The oldest result is released after eight condition sets");
        }
    }

    private List<String> labels(FilterOptionCache cache) throws Exception {
        return cache.options(Map.of()).get(5, TimeUnit.SECONDS).get("author").stream().map(FilterOption::label).toList();
    }

    private FilterOptionCache cache() {
        return new FilterOptionCache(catalog, catalog.fields());
    }

    public static class CountingValues extends FieldValueProvider {

        private static final AtomicInteger reads = new AtomicInteger();
        private static final AtomicReference<Thread> caller = new AtomicReference<>();

        @Override
        public Object read(Object row, Field field) throws IllegalAccessException {
            assertSame(caller.get(), Thread.currentThread(), "UI values are captured only on their owning thread");
            reads.incrementAndGet();
            return super.read(row, field);
        }
    }

    public static class CountingOptions extends ObservedOptionsProvider {

        private static final AtomicInteger calls = new AtomicInteger();
        private static final AtomicReference<Thread> worker = new AtomicReference<>();
        private static final AtomicBoolean block = new AtomicBoolean();
        private static CountDownLatch entered;
        private static CountDownLatch release;

        @Override
        public List<Object> options(FilterField field, List<?> rows) {
            calls.incrementAndGet();
            worker.set(Thread.currentThread());
            if (block.compareAndSet(true, false)) {
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Test did not release the background build");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            return super.options(field, rows);
        }
    }

    private static class TestRow {

        @FilterProperty(label = "Автор", editor = "choices", allowEmpty = false,
            valueProvider = CountingValues.class, optionsProvider = CountingOptions.class)
        private String author;

        @FilterProperty(label = "Прочтение", allowEmpty = false)
        private final Boolean read;

        private TestRow(String author, Boolean read) {
            this.author = author;
            this.read = read;
        }
    }
}
