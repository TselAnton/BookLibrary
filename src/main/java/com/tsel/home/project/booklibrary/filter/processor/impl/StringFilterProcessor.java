package com.tsel.home.project.booklibrary.filter.processor.impl;

import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessor;

/**
 * Подготавливает строки к текстовым условиям или справочному списку.
 * Пустые строки превращаются в маркер отсутствия; исходное написание вариантов сохраняется.
 */
@FilterTypes(value = String.class, editor = "text")
public class StringFilterProcessor implements FilterProcessor {

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
        return value == null || value instanceof String text && text.isBlank()
            ? MissingValue.EMPTY
            : value;
    }
}
