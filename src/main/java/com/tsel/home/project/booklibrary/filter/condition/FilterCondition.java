package com.tsel.home.project.booklibrary.filter.condition;

import com.tsel.home.project.booklibrary.filter.FilterField;

/**
 * Неизменяемое условие отбора, работающее с нормализованными значениями без JavaFX-контролов.
 * Один экземпляр безопасно используется и таблицей, и фоновым расчётом счётчиков.
 */
public interface FilterCondition {

    /**
     * Проверяет нормализованное значение без чтения DTO или изменения состояния.
     * @param value каноническое значение или маркер отсутствия
     * @return удовлетворяет ли значение условию
     */
    boolean matches(Object value);

    /**
     * Формирует читаемую подпись условия для тега с учётом метаданных поля.
     * @param field дескриптор, содержащий подписи и единицы измерения
     * @return текст условия без общего названия поля
     */
    String describe(FilterField field);
}
