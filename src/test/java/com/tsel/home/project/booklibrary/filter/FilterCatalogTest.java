package com.tsel.home.project.booklibrary.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.filter.condition.TextCondition;
import com.tsel.home.project.booklibrary.filter.condition.TextOperation;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.filter.model.FilterState;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessorRegistry;
import com.tsel.home.project.booklibrary.filter.processor.impl.BooleanFilterProcessor;
import com.tsel.home.project.booklibrary.filter.processor.impl.IntegerFilterProcessor;
import com.tsel.home.project.booklibrary.filter.processor.impl.NumericFilterProcessor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.CheckBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FilterCatalogTest extends UnitJavaFXTest {

    private final FilterCatalog catalog = new FilterCatalog(TestRow.class);
    private final TestRow first = new TestRow(true, "Фантастика", 2000, 99.5);
    private final TestRow second = new TestRow(false, "Детектив", 2010, 0.0);
    private final TestRow third = new TestRow(null, "Фантастика", null, null);
    private final List<TestRow> rows = List.of(first, second, third);

    @Test
    void testBooleanChoicesAndMissingValuesAreDistinct() {
        assertEquals(List.of(second), catalog.filter(rows, Map.of("flag", new ChoiceCondition(Set.of(false)))));
        assertEquals(List.of(third), catalog.filter(rows, Map.of("flag", new ChoiceCondition(Set.of(MissingValue.EMPTY)))));
        assertEquals(List.of(first, second), catalog.filter(rows, Map.of("flag", new ChoiceCondition(Set.of(true, false)))));
        FilterField flag = field("flag");
        assertEquals("Статус не указан", flag.optionLabel(MissingValue.EMPTY));
        assertNull(flag.processor().canonicalize(flag, new ChoiceCondition(Set.of(true, false, MissingValue.EMPTY))));
    }

    @Test
    void testFacetCountsExcludeTheirOwnConditionAndIncludeOtherConditions() {
        Map<String, FilterCondition> conditions = Map.of(
            "flag", new ChoiceCondition(Set.of(false)),
            "genre", new ChoiceCondition(Set.of("Фантастика"))
        );
        List<FilterOption> options = catalog.options(field("flag"), rows, conditions);
        assertEquals(List.of(1L, 0L, 1L), options.stream().map(FilterOption::count).toList());
        assertEquals(0, catalog.filter(rows, conditions).size());
        List<FilterOption> searched = catalog.options(field("flag"), rows, Map.of(
            "year", new NumericFilterProcessor().condition(NumericOperation.EQUALS, "2000", "")
        ));
        assertEquals(List.of(1L, 0L, 0L), searched.stream().map(FilterOption::count).toList());
    }

    @Test
    void testChoiceValuesSurviveTemporaryAbsenceFromTheCatalog() {
        List<FilterOption> options = catalog.options(field("genre"), List.of(), Map.of(
            "genre", new ChoiceCondition(Set.of("Исчезнувший жанр"))
        ));
        assertTrue(options.stream().anyMatch(option -> option.value().equals("Исчезнувший жанр") && option.count() == 0));
    }

    @Test
    void testNumericRangesAreInclusiveAndDoNotTreatMissingAsZero() {
        NumericFilterProcessor processor = new NumericFilterProcessor();
        assertEquals(List.of(first), catalog.filter(rows, Map.of("price", processor.condition(NumericOperation.RANGE, "99,50", "100"))));
        assertEquals(List.of(second), catalog.filter(rows, Map.of("price", processor.condition(NumericOperation.EQUALS, "0", ""))));
        assertEquals(List.of(third), catalog.filter(rows, Map.of("price", processor.condition(NumericOperation.EMPTY, "", ""))));
        assertEquals(List.of(first), catalog.filter(rows, Map.of("year", processor.condition(NumericOperation.RANGE, "", "2000"))));
        assertEquals(List.of(second), catalog.filter(rows, Map.of("year", processor.condition(NumericOperation.GREATER, "2000", ""))));
        assertTrue(processor.condition(NumericOperation.EQUALS, "0.10", "").matches(new BigDecimal("0.1")));
        assertFalse(processor.condition(NumericOperation.NOT_EQUALS, "0", "").matches(MissingValue.EMPTY));
        assertEquals(MissingValue.NOT_APPLICABLE, processor.normalize(MissingValue.NOT_APPLICABLE));
    }

    @Test
    void testNumericInputValidation() {
        NumericFilterProcessor processor = new NumericFilterProcessor();
        assertThrows(IllegalArgumentException.class, () -> processor.condition(NumericOperation.RANGE, "10", "1"));
        assertThrows(IllegalArgumentException.class, () -> processor.condition(NumericOperation.EQUALS, "abc", ""));
        assertThrows(IllegalArgumentException.class, () -> new IntegerFilterProcessor().condition(NumericOperation.EQUALS, "2,5", ""));
        assertNull(processor.condition(NumericOperation.RANGE, "", ""));
    }

    @Test
    void testTextConditionsKeepLiteralOperatorsAndNormalizeRussianText() {
        assertTrue(new TextCondition(TextOperation.CONTAINS, "еж AND OR").matches("ЁЖ and or"));
        assertTrue(new TextCondition(TextOperation.EQUALS, "Ёж").matches("еж"));
        assertFalse(new TextCondition(TextOperation.NOT_CONTAINS, "еж").matches("Ёжик"));
        assertFalse(new TextCondition(TextOperation.NOT_CONTAINS, "x").matches(MissingValue.EMPTY));
    }

    @ParameterizedTest
    @ValueSource(strings = {"бе", "бё", "БЕ", "БЁ"})
    void testTextFiltersIgnoreCaseAndRussianYoInBothDirections(String query) {
        FilterCatalog books = new FilterCatalog(BookDTO.class);
        BookDTO firstBook = BookDTO.builder().name("Книга бе").build();
        BookDTO secondBook = BookDTO.builder().name("Книга бё").build();
        BookDTO other = BookDTO.builder().name("Книга бы").author("бе").build();
        assertEquals(List.of(firstBook, secondBook), books.filter(List.of(firstBook, secondBook, other), Map.of(
            "name", new TextCondition(TextOperation.CONTAINS, query)
        )));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AND", "ИЛИ", "pages >= 300", "!read", "[текст]"})
    void testTextFilterTreatsLegacyCommandsAsLiteralText(String query) {
        FilterCatalog books = new FilterCatalog(BookDTO.class);
        BookDTO literal = BookDTO.builder().name("Книга " + query).build();
        BookDTO other = BookDTO.builder().name("Другая книга").pages(400).build();
        assertEquals(List.of(literal), books.filter(List.of(literal, other), Map.of(
            "name", new TextCondition(TextOperation.CONTAINS, query)
        )));
    }

    @Test
    void testDraftChangesDoNotMutateAppliedConditions() {
        FilterState state = new FilterState();
        state.begin();
        state.setDraft("flag", new ChoiceCondition(Set.of(false)));
        assertTrue(state.applied().isEmpty());
        state.apply();
        state.begin();
        state.setDraft("flag", new ChoiceCondition(Set.of(true)));
        state.begin();
        assertEquals(new ChoiceCondition(Set.of(false)), state.draft().get("flag"));
        assertThrows(UnsupportedOperationException.class, () -> state.applied().clear());
        state.remove("flag");
        assertTrue(state.applied().isEmpty());
    }

    @Test
    void testChoiceConditionsDefensivelyCopyTheirValues() {
        Set<Object> values = new HashSet<>(Set.of(true));
        ChoiceCondition condition = new ChoiceCondition(values);
        values.add(false);
        assertFalse(condition.matches(false));
    }

    @Test
    void testJavaTypesAreDeclaredOnProcessorsAndCanReuseAnExistingEditor() {
        FilterProcessorRegistry registry = FilterProcessorRegistry.defaults();
        assertSame(registry.resolve(Boolean.class), registry.resolve(boolean.class));
        assertSame(registry.resolve(Boolean.class), registry.resolve(CheckBox.class));
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        assertEquals(true, registry.resolve(CheckBox.class).normalize(checkBox));
        registry.register(new CustomFlagProcessor());
        FilterCatalog customCatalog = new FilterCatalog(CustomRow.class, registry);
        FilterField customField = customCatalog.fields().get(0);
        assertEquals("choices", customField.editor());
        assertEquals(true, customField.value(new CustomRow()));
        assertThrows(IllegalArgumentException.class, () -> registry.register(new BooleanFilterProcessor()));
        assertThrows(IllegalArgumentException.class, () -> registry.resolve(Thread.class));
        assertThrows(IllegalArgumentException.class, () -> new FilterCatalog(InvalidSettings.class));
    }

    @Test
    void testBooksWithoutCyclesAreNotUnfinishedOrUnknownCycles() {
        FilterCatalog books = new FilterCatalog(BookDTO.class);
        BookDTO standalone = BookDTO.builder().name("Без цикла").build();
        BookDTO unknown = BookDTO.builder().name("С циклом").cycleName("Цикл").build();
        assertEquals(List.of(standalone), books.filter(List.of(standalone, unknown), Map.of(
            "cycleEnded", new ChoiceCondition(Set.of(MissingValue.NOT_APPLICABLE))
        )));
        assertEquals(List.of(unknown), books.filter(List.of(standalone, unknown), Map.of(
            "cycleEnded", new ChoiceCondition(Set.of(MissingValue.EMPTY))
        )));
        assertEquals(List.of(), books.filter(List.of(standalone, unknown), Map.of("cycleEnded", new ChoiceCondition(Set.of(false)))));
        assertFalse(books.fields().stream().anyMatch(field -> field.id().equals("hasAnyAudioBookSite")));
    }

    private FilterField field(String name) {
        return catalog.fields().stream().filter(field -> field.id().equals(name)).findFirst().orElseThrow();
    }

    private static class TestRow {

        @FilterProperty(label = "Статус", emptyLabel = "Статус не указан")
        private final Boolean flag;

        @FilterProperty(label = "Жанр", editor = "choices")
        private final String genre;

        @FilterProperty(label = "Год")
        private final Integer year;

        @FilterProperty(label = "Цена")
        private final Double price;

        private TestRow(Boolean flag, String genre, Integer year, Double price) {
            this.flag = flag;
            this.genre = genre;
            this.year = year;
            this.price = price;
        }
    }

    private record CustomFlag(boolean value) {

    }

    private static class CustomRow {

        @FilterProperty(label = "Свой тип")
        private final CustomFlag flag = new CustomFlag(true);
    }

    @FilterTypes(value = CustomFlag.class, editor = "choices")
    private static class CustomFlagProcessor extends BooleanFilterProcessor {

        @Override
        public Object normalize(Object value) {
            return value instanceof CustomFlag flag ? flag.value() : super.normalize(value);
        }
    }

    private static class InvalidSettings {

        @FilterProperty(label = "Неправильная настройка", settings = "unknown=value")
        private Boolean flag;
    }
}
