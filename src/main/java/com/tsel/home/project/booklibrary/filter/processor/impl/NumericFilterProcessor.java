package com.tsel.home.project.booklibrary.filter.processor.impl;

import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.condition.NumericCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessor;
import java.math.BigDecimal;

/**
 * Приводит числовые значения к BigDecimal и проверяет ввод границ.
 * Принимает десятичную запятую и точку, не смешивает null с числовым нулём.
 */
@FilterTypes(
    value = {Double.class, double.class, Float.class, float.class, BigDecimal.class},
    editor = "number"
)
public class NumericFilterProcessor implements FilterProcessor {

    /**
     * Сообщает, допускается ли вычисление вариантов по снимкам в фоновом потоке.
     * @return true, если реализация не требует исходного DTO и доступа к JavaFX
     */
    @Override
    public boolean supportsSnapshots() {
        return true;
    }

    /**
     * Приводит исходное значение к каноническому виду без сохранения ссылок на UI-контролы.
     * @param value исходное значение поля
     * @return значение для сравнения или маркер отсутствия
     */
    @Override
    public Object normalize(Object value) {
        if (value == null) {
            return MissingValue.EMPTY;
        }
        return value instanceof MissingValue ? value : new BigDecimal(value.toString());
    }

    /**
     * Разбирает числовой ввод; принимает запятую и точку, проверяет порядок границ.
     * @param operation операция сравнения
     * @param firstText значение или нижняя граница
     * @param secondText верхняя граница диапазона
     * @return условие либо null, если ввод пуст
     * @throws IllegalArgumentException при некорректном числе или обратном диапазоне
     */
    public NumericCondition condition(NumericOperation operation, String firstText, String secondText) {
        if (operation == NumericOperation.EMPTY || operation == NumericOperation.NOT_EMPTY) {
            return new NumericCondition(operation, null, null);
        }
        BigDecimal first = parse(firstText);
        BigDecimal second = operation == NumericOperation.RANGE ? parse(secondText) : null;
        if (first == null && second == null) {
            return null;
        }
        if (first != null && second != null && first.compareTo(second) > 0) {
            throw new IllegalArgumentException("Значение «От» не должно превышать «До»");
        }
        return new NumericCondition(operation, first, second);
    }

    protected boolean integral() {
        return false;
    }

    private BigDecimal parse(String text) {
        if (text.isBlank()) {
            return null;
        }
        try {
            BigDecimal number = new BigDecimal(text.trim().replace(',', '.'));
            if (integral() && number.stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException("Введите целое число");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите корректное число", e);
        }
    }
}
