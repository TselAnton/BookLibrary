package com.tsel.home.project.booklibrary.filter.provider.impl;

import com.tsel.home.project.booklibrary.filter.provider.FilterValueProvider;
import java.lang.reflect.Field;

/**
 * Читает поле DTO через reflection; доступность поля настраивается дескриптором фильтра.
 */
public class FieldValueProvider implements FilterValueProvider {

    /**
     * Извлекает исходное значение на потоке владельца DTO до фоновой обработки.
     * @param row исходная строка данных
     * @param field поле для чтения
     * @return значение поля либо специальный маркер
     * @throws IllegalAccessException при невозможности прочитать поле
     */
    @Override
    public Object read(Object row, Field field) throws IllegalAccessException {
        return field.get(row);
    }
}
