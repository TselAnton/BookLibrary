package com.tsel.home.project.booklibrary.filter.cache;

import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.filter.model.FilterRow;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Кеширует домены вариантов и восемь последних наборов счётчиков по условиям.
 * Все публичные методы вызываются только создавшим кеш потоком, обычно JavaFX-потоком.
 * Фоновый исполнитель читает только неизменяемые снимки и собственные локальные карты.
 * Поэтому concurrent-карты не требуются; ограничение LRU и close управляют удержанием данных.
 * Ключи условий должны оставаться сильными: weak-ключи удалялись бы при обычной сборке мусора.
 */
public class FilterOptionCache implements AutoCloseable {

    private static final int MAX_CACHED_QUERIES = 8;

    private final Thread ownerThread = Thread.currentThread();
    private final FilterCatalog catalog;
    private final List<FilterField> optionFields;
    private final Map<Map<String, FilterCondition>, CompletableFuture<Map<String, List<FilterOption>>>> queries =
        new LinkedHashMap<>(16, 0.75f, true);

    private final ExecutorService worker = Executors.newSingleThreadExecutor(action -> {
        Thread thread = new Thread(action, "booklibrary-filter-cache");
        thread.setDaemon(true);
        return thread;
    });

    private CompletableFuture<Index> index;
    private List<FilterRow> previousRows;
    private Map<String, List<Object>> previousOptions;
    private boolean closed;

    /**
     * Создаёт кеш и закрепляет его публичный API за вызывающим потоком.
     * @param catalog каталог дескрипторов
     * @param optionFields поля, редакторам которых нужны списки и счётчики
     */
    public FilterOptionCache(FilterCatalog catalog, List<FilterField> optionFields) {
        this.catalog = catalog;
        this.optionFields = List.copyOf(optionFields);
    }

    /**
     * Захватывает нормализованные значения на потоке владельца и строит индекс в фоне.
     * При одинаковом снимке сохраняет прогретые результаты, при изменении отменяет старые.
     * @param rows полный актуальный каталог DTO
     */
    public void replaceRows(List<?> rows) {
        checkOwnerThread();
        if (closed) {
            return;
        }
        List<FilterRow> snapshots = new ArrayList<>();
        for (Object row : rows) {
            Map<String, Object> values = new LinkedHashMap<>();
            catalog.fields().forEach(field -> values.put(field.id(), field.value(row)));
            snapshots.add(new FilterRow(values));
        }
        Map<String, List<Object>> suppliedOptions = new LinkedHashMap<>();
        for (FilterField field : optionFields) {
            if (!field.processor().supportsSnapshots() || !field.optionsProvider().supportsSnapshots()) {
                suppliedOptions.put(field.id(), List.copyOf(field.optionValues(rows)));
            }
        }
        List<FilterRow> capturedRows = List.copyOf(snapshots);
        Map<String, List<Object>> capturedOptions = Map.copyOf(suppliedOptions);
        if (index != null && capturedRows.equals(previousRows) && capturedOptions.equals(previousOptions)) {
            return;
        }
        if (index != null) {
            index.cancel(false);
        }
        queries.values().forEach(query -> query.cancel(false));
        queries.clear();
        previousRows = capturedRows;
        previousOptions = capturedOptions;
        index = CompletableFuture.supplyAsync(() -> {
            Map<String, List<Object>> domains = new LinkedHashMap<>();
            optionFields.forEach(field -> domains.put(field.id(), capturedOptions.containsKey(field.id())
                ? capturedOptions.get(field.id())
                : List.copyOf(field.optionValues(capturedRows)))
            );
            return new Index(capturedRows, Map.copyOf(domains));
        }, worker);
    }

    /**
     * Возвращает фоновый расчёт или переиспользует результат последних условий.
     * LRU удерживает не более восьми запросов; при смене каталога они инвалидируются.
     * @param conditions неизменяемые по смыслу типизированные условия
     * @return future карты вариантов; после close возвращается ошибочный future
     */
    public CompletableFuture<Map<String, List<FilterOption>>> options(Map<String, FilterCondition> conditions) {
        checkOwnerThread();
        if (closed || index == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Кеш фильтров недоступен"));
        }
        Map<String, FilterCondition> query = Map.copyOf(conditions);
        CompletableFuture<Map<String, List<FilterOption>>> cached = queries.get(query);
        if (cached != null) {
            return cached;
        }
        CompletableFuture<Map<String, List<FilterOption>>> result = index.thenApplyAsync(snapshot -> {
            Map<String, List<FilterOption>> options = new LinkedHashMap<>();
            optionFields.forEach(field -> options.put(field.id(), catalog.optionsFromValues(
                field, snapshot.domains().get(field.id()), snapshot.rows(), query
            )));
            return Map.copyOf(options);
        }, worker);
        queries.put(query, result);
        if (queries.size() > MAX_CACHED_QUERIES) {
            Map<String, FilterCondition> oldest = queries.keySet().iterator().next();
            queries.remove(oldest).cancel(false);
        }
        return result;
    }

    /**
     * Отменяет фоновые запросы, очищает ссылки на индекс и завершает исполнитель.
     * Вызывается потоком владельца; повторный вызов безопасен.
     */
    @Override
    public void close() {
        checkOwnerThread();
        closed = true;
        if (index != null) {
            index.cancel(false);
            index = null;
        }
        queries.values().forEach(query -> query.cancel(false));
        queries.clear();
        previousRows = List.of();
        previousOptions = Map.of();
        worker.shutdownNow();
    }

    private void checkOwnerThread() {
        if (Thread.currentThread() != ownerThread) {
            throw new IllegalStateException("Кеш фильтров доступен только создавшему его потоку");
        }
    }

    private record Index(List<FilterRow> rows, Map<String, List<Object>> domains) {

    }
}
