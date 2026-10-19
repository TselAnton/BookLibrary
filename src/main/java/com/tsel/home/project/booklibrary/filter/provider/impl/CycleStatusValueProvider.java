package com.tsel.home.project.booklibrary.filter.provider.impl;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import java.lang.reflect.Field;

/**
 * Отделяет книгу без цикла от существующего цикла с неизвестной завершённостью.
 * Для книги без цикла возвращает NOT_APPLICABLE вместо ложного или пустого признака.
 */
public class CycleStatusValueProvider extends FieldValueProvider {

    /**
     * Извлекает исходное значение на потоке владельца DTO до фоновой обработки.
     * @param row исходная строка данных
     * @param field поле для чтения
     * @return значение поля либо специальный маркер
     * @throws IllegalAccessException при невозможности прочитать поле
     */
    @Override
    public Object read(Object row, Field field) throws IllegalAccessException {
        if (row instanceof BookDTO book && book.getCycleName() == null) {
            return MissingValue.NOT_APPLICABLE;
        }
        return super.read(row, field);
    }
}
