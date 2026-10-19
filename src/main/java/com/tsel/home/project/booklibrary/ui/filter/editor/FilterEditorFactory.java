package com.tsel.home.project.booklibrary.ui.filter.editor;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;

/**
 * Создаёт редактор по дескриптору без привязки основного контроллера к Java-типу.
 */
@FunctionalInterface
public interface FilterEditorFactory {

    /**
     * Создаёт редактор с исходным условием и уведомлением об изменении черновика.
     * @param field дескриптор фильтра
     * @param condition исходное условие или null
     * @param changed callback пересчёта черновика при изменениях
     * @return новый редактор
     */
    FilterEditor create(FilterField field, FilterCondition condition, Runnable changed);
}
