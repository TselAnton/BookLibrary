package com.tsel.home.project.booklibrary.filter.model;

import java.util.Map;

/**
 * Хранит неизменяемый снимок нормализованных значений для фоновых вычислений.
 * Снимок не должен содержать JavaFX-контролы или другие объекты, привязанные к UI-потоку.
 * @param values значения по идентификаторам полей
 */
public record FilterRow(Map<String, Object> values) {

    /**
     * Создаёт защитную копию карты, отделяя снимок от последующих изменений источника.
     */
    public FilterRow {
        values = Map.copyOf(values);
    }
}
