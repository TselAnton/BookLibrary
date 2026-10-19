package com.tsel.home.project.booklibrary.ui.filter.editor;

import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import java.util.List;
import javafx.scene.Node;

/**
 * Предоставляет UI-редактор одного условия и уведомление о правках черновика.
 * Методы редактора вызываются только в JavaFX-потоке.
 */
public interface FilterEditor {

    /**
     * Возвращает переиспользуемый корневой узел редактора.
     * @return узел, размещаемый внутри группы фильтра
     */
    Node node();

    /**
     * Возвращает типизированное условие текущего ввода; пустой ввод не ограничивает выборку.
     * @return условие либо null
     * @throws IllegalArgumentException при некорректном вводе
     */
    FilterCondition condition();

    /**
     * Восстанавливает применённое условие без создания нового редактора.
     * @param condition сохранённое условие или null для сброса
     * @return true при поддержке восстановления; иначе панель создаст редактор заново
     */
    default boolean restore(FilterCondition condition) {
        return false;
    }

    /**
     * Сообщает, нужны ли редактору варианты и их фоновые счётчики.
     * @return true для редакторов справочных списков
     */
    default boolean requiresOptions() {
        return false;
    }

    /**
     * Обновляет варианты и счётчики, сохраняя текущие выбранные значения.
     * @param options актуальные канонические варианты с подписями и количеством книг
     */
    default void refresh(List<FilterOption> options) {
        // Editors without choice rows do not display facet counts.
    }
}
