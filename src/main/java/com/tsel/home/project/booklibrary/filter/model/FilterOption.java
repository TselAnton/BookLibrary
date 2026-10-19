package com.tsel.home.project.booklibrary.filter.model;

/**
 * Содержит вариант списка и количество книг с учётом остальных условий.
 * @param value каноническое значение для сравнения
 * @param label отображаемая подпись
 * @param count число совпадений без учёта собственного условия поля
 */
public record FilterOption(Object value, String label, long count) {

}
