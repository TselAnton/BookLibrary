package com.tsel.home.project.booklibrary.filter.provider.impl;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.provider.FilterOptionsProvider;
import java.util.List;

/**
 * Собирает уникальные непустые значения из каталога и сортирует их по отображаемой подписи.
 * Использует FilterField.value, поэтому поддерживает и исходные DTO, и фоновые снимки.
 */
public class ObservedOptionsProvider implements FilterOptionsProvider {

    /**
     * Сообщает, допускается ли вычисление вариантов по снимкам в фоновом потоке.
     * @return true, если реализация не требует исходного DTO и доступа к JavaFX
     */
    @Override
    public boolean supportsSnapshots() {
        return true;
    }

    /**
     * Собирает варианты без счётчиков; пустые значения добавляет дескриптор согласно метаданным.
     * @param field дескриптор фильтра
     * @param rows исходные строки или поддерживаемые снимки
     * @return канонические значения для списка
     */
    @Override
    public List<Object> options(FilterField field, List<?> rows) {
        return rows.stream()
            .map(field::value)
            .filter(value -> !(value instanceof MissingValue))
            .distinct()
            .sorted((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(field.optionLabel(left), field.optionLabel(right)))
            .toList();
    }
}
