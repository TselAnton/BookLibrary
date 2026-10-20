package com.tsel.home.project.booklibrary.ui.filter.editor;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.ui.filter.editor.impl.ChoiceFilterEditor;
import com.tsel.home.project.booklibrary.ui.filter.editor.impl.NumericFilterEditor;
import com.tsel.home.project.booklibrary.ui.filter.editor.impl.TextFilterEditor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сопоставляет идентификаторы редакторов с фабриками.
 * Позволяет добавить новый UI-компонент без изменения контроллера библиотеки.
 */
public class FilterEditorRegistry {

    private final Map<String, FilterEditorFactory> factories = new LinkedHashMap<>();

    /**
     * Создаёт независимый реестр со стандартными реализациями приложения.
     * @return реестр, который можно дополнить перед созданием каталога
     */
    public static FilterEditorRegistry defaults() {
        FilterEditorRegistry registry = new FilterEditorRegistry();
        registry.register("choices", ChoiceFilterEditor::new);
        registry.register("text", TextFilterEditor::new);
        registry.register("number", NumericFilterEditor::new);
        return registry;
    }

    /**
     * Регистрирует обработчик, запрещая повторные или некорректные назначения.
     * @throws IllegalArgumentException при конфликте регистрации
     */
    public void register(String name, FilterEditorFactory factory) {
        if (name.isBlank() || factories.putIfAbsent(name, factory) != null) {
            throw new IllegalArgumentException("Некорректный или повторный редактор фильтра: " + name);
        }
    }

    /**
     * Создаёт редактор с исходным условием и уведомлением об изменении черновика.
     * @param field дескриптор фильтра
     * @param condition исходное условие или null
     * @param changed callback пересчёта черновика при изменениях
     * @return новый редактор
     */
    public FilterEditor create(FilterField field, FilterCondition condition, Runnable changed) {
        FilterEditorFactory factory = factories.get(field.editor());
        if (factory == null) {
            throw new IllegalArgumentException("Не зарегистрирован редактор «" + field.editor() + "» для фильтра «" + field.label() + "»");
        }
        return factory.create(field, condition, changed);
    }
}
