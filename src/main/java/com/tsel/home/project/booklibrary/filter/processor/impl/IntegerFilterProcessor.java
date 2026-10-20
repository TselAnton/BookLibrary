package com.tsel.home.project.booklibrary.filter.processor.impl;

import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;

/**
 * Использует общий числовой редактор, дополнительно запрещая дробный ввод для целых Java-типов.
 */
@FilterTypes(
    editor = "number",
    value = {Integer.class, int.class, Long.class, long.class, Short.class, short.class}
)
public class IntegerFilterProcessor extends NumericFilterProcessor {

    @Override
    protected boolean integral() {
        return true;
    }
}
