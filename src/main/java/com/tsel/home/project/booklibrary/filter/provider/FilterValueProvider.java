package com.tsel.home.project.booklibrary.filter.provider;

import java.lang.reflect.Field;

/**
 * Извлекает исходное значение поля перед нормализацией процессором.
 * Вызывается в UI-потоке, поэтому может читать JavaFX-контролы и связи DTO.
 */
public interface FilterValueProvider {

    /**
     * Извлекает исходное значение на потоке владельца DTO до фоновой обработки.
     * @param row исходная строка данных
     * @param field поле для чтения
     * @return значение поля либо специальный маркер
     * @throws IllegalAccessException при невозможности прочитать поле
     */
    Object read(Object row, Field field) throws IllegalAccessException;
}
