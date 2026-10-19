package com.tsel.home.project.booklibrary.ui.filter.editor;

import com.tsel.home.project.booklibrary.ui.SageTheme;
import java.util.function.Function;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

/**
 * Настраивает единое представление операций в ComboBox, включая подписи и подсказки.
 */
public final class FilterEditorControls {

    private FilterEditorControls() {}

    /**
     * Настраивает подписи, ограниченную ширину и подсказки списка операций.
     * @param <T> тип операции
     * @param combo настраиваемый список
     * @param label функция локализованной подписи
     */
    public static <T> void configureOperations(ComboBox<T> combo, Function<T, String> label) {
        SageTheme.constrainComboBox(combo);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : label.apply(value);
            }

            @Override
            public T fromString(String text) {
                return combo.getItems().stream().filter(value -> label.apply(value).equals(text)).findFirst().orElse(null);
            }
        });
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                String text = empty || value == null ? null : label.apply(value);
                setText(text);
                setTooltip(text == null ? null : new Tooltip(text));
                setMinWidth(0);
                setPrefWidth(Math.max(0, list.getWidth() - 18));
                setMaxWidth(USE_PREF_SIZE);
            }
        });
    }
}
