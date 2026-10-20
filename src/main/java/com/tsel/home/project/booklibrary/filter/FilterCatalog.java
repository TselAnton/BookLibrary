package com.tsel.home.project.booklibrary.filter;

import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessorRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Обнаруживает доступные фильтры по метаданным полей и применяет типизированные условия.
 * Условия разных полей объединяются через И; счётчик вариантов игнорирует условие своего поля.
 * @param fields упорядоченные дескрипторы фильтров
 */
public record FilterCatalog(List<FilterField> fields) {

    /**
     * Обнаруживает доступные аннотированные поля и упорядочивает их по метаданным.
     */
    public FilterCatalog(Class<?> rowType) {
        this(rowType, FilterProcessorRegistry.defaults());
    }

    /**
     * Обнаруживает доступные аннотированные поля и упорядочивает их по метаданным.
     * @param rowType класс DTO с аннотированными полями
     * @param registry реестр стандартных и дополнительных Java-типов
     */
    public FilterCatalog(Class<?> rowType, FilterProcessorRegistry registry) {
        this(Arrays.stream(rowType.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(FilterProperty.class))
            .filter(field -> field.getAnnotation(FilterProperty.class).visible())
            .map(field -> new FilterField(field, registry.resolve(field.getType())))
            .sorted(Comparator.comparingInt((FilterField field) -> field.metadata().order())
                .thenComparing(FilterField::id))
            .toList());
    }

    /**
     * Применяет условия разных полей через И, при необходимости исключая указанное поле.
     * Исключение используется при расчёте его собственных счётчиков.
     * @param <T> тип строки данных
     * @param rows исходные строки
     * @param conditions условия по идентификаторам полей
     * @return отобранные строки с сохранением исходного порядка
     */
    public <T> List<T> filter(List<T> rows, Map<String, FilterCondition> conditions) {
        return filter(rows, conditions, "");
    }

    /**
     * Применяет условия разных полей через И, при необходимости исключая указанное поле.
     * Исключение используется при расчёте его собственных счётчиков.
     * @param <T> тип строки данных
     * @param rows исходные строки
     * @param conditions условия по идентификаторам полей
     * @param except идентификатор поля, чьё условие нужно исключить; пустая строка ничего не исключает
     * @return отобранные строки с сохранением исходного порядка
     */
    public <T> List<T> filter(List<T> rows, Map<String, FilterCondition> conditions,
        String except) {
        return rows.stream()
            .filter(row -> fields.stream().allMatch(field -> field.id().equals(except)
                || !conditions.containsKey(field.id()) || conditions.get(field.id())
                .matches(field.value(row))))
            .toList();
    }

    /**
     * Получает варианты из каталога и считает книги с учётом остальных условий.
     * @param field поле справочного списка
     * @param rows полный каталог строк
     * @param conditions применяемые условия
     * @return варианты с независимыми от собственной группы счётчиками
     */
    public List<FilterOption> options(FilterField field, List<?> rows,
        Map<String, FilterCondition> conditions) {
        return optionsFromValues(field, field.optionValues(rows), rows, conditions);
    }

    /**
     * Рассчитывает счётчики по готовому домену, исключая собственное условие поля.
     * Сохраняет выбранные варианты, временно исчезнувшие из каталога, с нулевым счётчиком.
     * @param field поле справочного списка
     * @param optionValues заранее вычисленные канонические варианты
     * @param rows актуальный снимок каталога
     * @param conditions черновые или применённые условия
     * @return варианты с актуальными счётчиками
     */
    public List<FilterOption> optionsFromValues(FilterField field, List<Object> optionValues,
        List<?> rows, Map<String, FilterCondition> conditions) {
        List<?> candidates = filter(rows, conditions, field.id());
        List<Object> values = new ArrayList<>(optionValues);
        if (conditions.get(field.id()) instanceof ChoiceCondition choice) {
            choice.values().stream().filter(value -> !values.contains(value)).forEach(values::add);
        }
        Map<Object, Long> counts = new LinkedHashMap<>();
        for (Object row : candidates) {
            counts.merge(field.value(row), 1L, Long::sum);
        }
        return values.stream()
            .map(value -> new FilterOption(value, field.optionLabel(value),
                counts.getOrDefault(value, 0L)))
            .toList();
    }
}
