package com.tsel.home.project.booklibrary.ui.filter.editor.impl;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.condition.TextCondition;
import com.tsel.home.project.booklibrary.filter.condition.TextOperation;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditor;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditorControls;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Редактирует буквальный текст и способ сравнения; проверки заполненности скрывают поле ввода.
 */
public class TextFilterEditor implements FilterEditor {

    private final ComboBox<TextOperation> operation = new ComboBox<>();
    private final TextField query = new TextField();
    private final VBox content = new VBox(8, operation, query);

    /**
     * Создаёт редактор и восстанавливает исходное условие.
     * @param field метаданные и процессор поля
     * @param condition исходное условие либо null
     * @param changed уведомление о правках черновика
     */
    public TextFilterEditor(FilterField field, FilterCondition condition, Runnable changed) {
        operation.getItems().setAll(Arrays.stream(TextOperation.values())
            .filter(value -> field.metadata().allowEmpty() || value != TextOperation.EMPTY && value != TextOperation.NOT_EMPTY)
            .toList());
        operation.setMaxWidth(Double.MAX_VALUE);
        operation.setAccessibleText("Условие: " + field.label());
        operation.setId("filter-" + field.id() + "-operation");
        FilterEditorControls.configureOperations(operation, value -> switch (value) {
            case EMPTY -> field.metadata().emptyLabel();
            case NOT_EMPTY -> field.metadata().notEmptyLabel();
            default -> value.toString();
        });
        query.setPromptText("Введите текст…");
        query.setAccessibleText(field.label());
        query.setId("filter-" + field.id() + "-value");
        TextCondition initial = condition instanceof TextCondition text ? text : null;
        operation.setValue(initial == null ? TextOperation.CONTAINS : initial.operation());
        query.setText(initial == null ? "" : initial.query());
        updateFields();
        operation.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFields();
            changed.run();
        });
        query.textProperty().addListener((observable, oldValue, newValue) -> changed.run());
    }

    /**
     * Возвращает переиспользуемый корневой узел редактора.
     * @return узел, размещаемый внутри группы фильтра
     */
    @Override
    public Node node() {
        return content;
    }

    /**
     * Возвращает типизированное условие текущего ввода; пустой ввод не ограничивает выборку.
     * @return условие либо null
     * @throws IllegalArgumentException при некорректном вводе
     */
    @Override
    public FilterCondition condition() {
        if (operation.getValue() == TextOperation.EMPTY || operation.getValue() == TextOperation.NOT_EMPTY) {
            return new TextCondition(operation.getValue(), "");
        }
        return query.getText().isBlank() ? null : new TextCondition(operation.getValue(), query.getText().trim());
    }

    /**
     * Восстанавливает применённое условие без создания нового редактора.
     * @param condition сохранённое условие или null для сброса
     * @return true при поддержке восстановления; иначе панель создаст редактор заново
     */
    @Override
    public boolean restore(FilterCondition condition) {
        TextCondition initial = condition instanceof TextCondition text ? text : null;
        operation.setValue(initial == null ? TextOperation.CONTAINS : initial.operation());
        query.setText(initial == null ? "" : initial.query());
        updateFields();
        return true;
    }

    private void updateFields() {
        boolean visible = operation.getValue() != TextOperation.EMPTY && operation.getValue() != TextOperation.NOT_EMPTY;
        query.setVisible(visible);
        query.setManaged(visible);
    }
}
