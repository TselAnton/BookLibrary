package com.tsel.home.project.booklibrary.ui.filter.editor.impl;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.filter.processor.impl.NumericFilterProcessor;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditor;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditorControls;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Редактирует числовое сравнение или диапазон с проверкой допустимого ввода процессором.
 * Для обязательных полей исключает проверки заполненности; специальные подписи берёт из метаданных.
 */
public class NumericFilterEditor implements FilterEditor {

    private final ComboBox<NumericOperation> operation = new ComboBox<>();
    private final TextField first = new TextField();
    private final TextField second = new TextField();
    private final HBox values = new HBox(8, first, second);
    private final VBox content = new VBox(8, operation, values);
    private final NumericFilterProcessor processor;

    /**
     * Создаёт редактор и восстанавливает исходное условие.
     * @param field метаданные и процессор поля
     * @param condition исходное условие либо null
     * @param changed уведомление о правках черновика
     */
    public NumericFilterEditor(FilterField field, FilterCondition condition, Runnable changed) {
        if (!(field.processor() instanceof NumericFilterProcessor numericProcessor)) {
            throw new IllegalArgumentException("Числовому редактору нужен числовой процессор: " + field.label());
        }
        processor = numericProcessor;
        operation.getItems().setAll(Arrays.stream(NumericOperation.values())
            .filter(value -> field.metadata().allowEmpty() || value != NumericOperation.EMPTY && value != NumericOperation.NOT_EMPTY)
            .toList());
        operation.setMaxWidth(Double.MAX_VALUE);
        operation.setAccessibleText("Условие: " + field.label());
        operation.setId("filter-" + field.id() + "-operation");
        FilterEditorControls.configureOperations(operation, value -> switch (value) {
            case EMPTY -> field.metadata().emptyLabel();
            case NOT_EMPTY -> field.metadata().notEmptyLabel();
            default -> value.toString();
        });
        first.setMinWidth(0);
        second.setMinWidth(0);
        first.setAccessibleText(field.label() + ": значение или нижняя граница");
        second.setAccessibleText(field.label() + ": верхняя граница");
        first.setId("filter-" + field.id() + "-first");
        second.setId("filter-" + field.id() + "-second");
        HBox.setHgrow(first, Priority.ALWAYS);
        HBox.setHgrow(second, Priority.ALWAYS);
        if (!field.metadata().unit().isBlank()) {
            Label unit = new Label(field.metadata().unit());
            unit.getStyleClass().add("caption");
            content.getChildren().add(unit);
        }
        NumericCondition initial = condition instanceof NumericCondition number ? number : null;
        operation.setValue(initial == null ? NumericOperation.RANGE : initial.operation());
        if (initial != null) {
            first.setText(initial.first() == null ? "" : initial.first().toPlainString());
            second.setText(initial.second() == null ? "" : initial.second().toPlainString());
        }
        updateFields();
        operation.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFields();
            changed.run();
        });
        first.textProperty().addListener((observable, oldValue, newValue) -> changed.run());
        second.textProperty().addListener((observable, oldValue, newValue) -> changed.run());
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
        return processor.condition(operation.getValue(), first.getText(), second.getText());
    }

    /**
     * Восстанавливает применённое условие без создания нового редактора.
     * @param condition сохранённое условие или null для сброса
     * @return true при поддержке восстановления; иначе панель создаст редактор заново
     */
    @Override
    public boolean restore(FilterCondition condition) {
        NumericCondition initial = condition instanceof NumericCondition number ? number : null;
        operation.setValue(initial == null ? NumericOperation.RANGE : initial.operation());
        first.setText(initial == null || initial.first() == null ? "" : initial.first().toPlainString());
        second.setText(initial == null || initial.second() == null ? "" : initial.second().toPlainString());
        updateFields();
        return true;
    }

    private void updateFields() {
        boolean visible = operation.getValue() != NumericOperation.EMPTY && operation.getValue() != NumericOperation.NOT_EMPTY;
        values.setVisible(visible);
        values.setManaged(visible);
        boolean range = operation.getValue() == NumericOperation.RANGE;
        second.setVisible(range);
        second.setManaged(range);
        first.setPromptText(range ? "От" : "Значение");
        second.setPromptText("До");
    }
}
