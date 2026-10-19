package com.tsel.home.project.booklibrary.filter.provider;

import com.tsel.home.project.booklibrary.filter.FilterField;
import java.util.List;

/**
 * Поставляет канонические значения справочного списка без счётчиков.
 * Фоновая обработка разрешается только явным подтверждением поддержки снимков.
 */
public interface FilterOptionsProvider {

    /**
     * Собирает варианты без счётчиков; пустые значения добавляет дескриптор согласно метаданным.
     * @param field дескриптор фильтра
     * @param rows исходные строки или поддерживаемые снимки
     * @return канонические значения для списка
     */
    List<Object> options(FilterField field, List<?> rows);

    /**
     * Сообщает, допускается ли вычисление вариантов по снимкам в фоновом потоке.
     * @return true, если реализация не требует исходного DTO и доступа к JavaFX
     */
    default boolean supportsSnapshots() {
        return false;
    }
}
