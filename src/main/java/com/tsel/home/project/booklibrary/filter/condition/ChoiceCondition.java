package com.tsel.home.project.booklibrary.filter.condition;

import com.tsel.home.project.booklibrary.filter.FilterField;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Отбирает одно или несколько точных значений по правилу ИЛИ внутри поля.
 * Пустой набор означает отсутствие ограничения.
 * @param values выбранные канонические значения; подписи UI не используются как идентификаторы
 */
public record ChoiceCondition(Set<Object> values) implements FilterCondition {

    /**
     * Копирует выбранные значения, чтобы изменение UI-набора не влияло на условие.
     */
    public ChoiceCondition {
        values = Set.copyOf(values);
    }

    /**
     * Проверяет нормализованное значение без чтения DTO или изменения состояния.
     * @param value каноническое значение или маркер отсутствия
     * @return удовлетворяет ли значение условию
     */
    @Override
    public boolean matches(Object value) {
        return values.isEmpty() || values.contains(value);
    }

    /**
     * Формирует читаемую подпись условия для тега с учётом метаданных поля.
     * @param field дескриптор, содержащий подписи и единицы измерения
     * @return текст условия без общего названия поля
     */
    @Override
    public String describe(FilterField field) {
        return values.stream()
            .map(field::optionLabel)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.joining(", "));
    }
}
