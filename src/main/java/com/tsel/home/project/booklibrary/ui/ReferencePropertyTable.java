package com.tsel.home.project.booklibrary.ui;

import com.tsel.home.project.booklibrary.dao.annotation.Property;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

/**
 * Таблица свойств справочной записи: подписи берутся из @Property, редактор — из типа поля.
 * Поля UUID доступны только для чтения и копирования.
 */
public final class ReferencePropertyTable extends TableView<ReferencePropertyTable.PropertyRow> {

    private final Map<String, Node> editors = new LinkedHashMap<>();

    public ReferencePropertyTable(Class<?> entityClass, Runnable onEdit) {
        setId("referenceProperties");
        getStyleClass().add("reference-property-table");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<PropertyRow, String> property = new TableColumn<>("Свойство");
        property.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(value.getValue().label()));
        property.setMinWidth(170);
        property.setPrefWidth(180);
        property.setMaxWidth(200);
        property.setSortable(false);
        property.setReorderable(false);
        TableColumn<PropertyRow, Node> value = new TableColumn<>("Значение");
        value.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().editor()));
        value.setSortable(false);
        value.setReorderable(false);
        value.setMinWidth(240);
        value.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Node editor, boolean empty) {
                super.updateItem(editor, empty);
                setText(null);
                setGraphic(empty ? null : editor);
                if (!empty && editor instanceof Region region) {
                    region.prefWidthProperty().bind(widthProperty().subtract(16));
                    region.setMaxWidth(Double.MAX_VALUE);
                }
            }
        });
        getColumns().add(property);
        getColumns().add(value);
        for (Field field : entityClass.getDeclaredFields()) {
            Property annotation = field.getAnnotation(Property.class);
            if (annotation == null) {
                continue;
            }
            String label = annotation.value().substring(0, 1).toUpperCase(Locale.ROOT) + annotation.value().substring(1);
            Node editor = createEditor(field.getType(), label, onEdit);
            editor.setId("reference" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1));
            editor.setAccessibleText(label);
            editors.put(field.getName(), editor);
            getItems().add(new PropertyRow(label, editor));
        }
        double height = getItems().size() * 46 + 36;
        setMinHeight(height);
        setPrefHeight(height);
        setMaxHeight(height);
    }

    public void setValue(String property, Object value) {
        Node editor = editors.get(property);
        if (editor instanceof TextField text) {
            text.setText(value == null ? "" : value.toString());
        } else if (editor instanceof CheckBox flag) {
            flag.setSelected(Boolean.TRUE.equals(value));
        } else if (editor instanceof Label label) {
            label.setText(value == null ? "" : value.toString());
        }
    }

    public String text(String property) {
        return ((TextField) editors.get(property)).getText();
    }

    public boolean flag(String property) {
        return ((CheckBox) editors.get(property)).isSelected();
    }

    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        editors.forEach((property, editor) -> {
            if (editor instanceof TextField text) {
                values.put(property, text.getText());
            } else if (editor instanceof CheckBox flag) {
                values.put(property, flag.isSelected());
            } else if (editor instanceof Label label) {
                values.put(property, label.getText());
            }
        });
        return values;
    }

    private Node createEditor(Class<?> type, String label, Runnable onEdit) {
        if (type == Boolean.class || type == boolean.class) {
            CheckBox flag = new CheckBox("Нет");
            flag.selectedProperty().addListener((observable, previous, current) -> {
                flag.setText(current ? "Да" : "Нет");
                onEdit.run();
            });
            return flag;
        }
        if (type == String.class || type == Integer.class || type == int.class || type == UUID.class) {
            TextField text = new TextField();
            text.setMaxWidth(Double.MAX_VALUE);
            if (type == UUID.class) {
                text.setEditable(false);
                text.getStyleClass().add("reference-read-only");
                text.setTooltip(new Tooltip("Идентификатор нельзя изменить. Значение можно выделить и скопировать."));
            } else {
                if (type == Integer.class || type == int.class) {
                    text.setTextFormatter(new TextFormatter<String>(change ->
                        change.getControlNewText().matches("[0-9]*") ? change : null));
                    text.setPromptText("Целое число от 0");
                }
                text.textProperty().addListener((observable, previous, current) -> onEdit.run());
            }
            return text;
        }
        Label value = new Label();
        value.setAccessibleText(label);
        return value;
    }

    public record PropertyRow(String label, Node editor) {}
}
