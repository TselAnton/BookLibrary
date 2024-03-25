package com.tsel.home.project.booklibrary.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Данный класс позволяет создать CheckBox колонку с возможностью отслеживать евенты самих CheckBox
 */
@SuppressWarnings("unchecked")
public class CustomCheckBoxTableCell<S,T> extends TableCell<S,T> {

    private final CheckBox checkBox;
    private String id;

    private boolean showLabel;

    private ObservableValue<Boolean> booleanProperty;

    public CustomCheckBoxTableCell() {
        this.getStyleClass().add("check-box-table-cell");

        this.checkBox = new CheckBox();
        this.checkBox.setOnAction(this::handleActionEvent);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
    }

    protected void handleActionEvent(ActionEvent event) {}

    public void setCheckBoxId(String id) {
        this.id = id;
    }

    public String getCheckBoxId() {
        return this.id;
    }

    public boolean isCheckBoxSelected() {
        return this.checkBox.isSelected();
    }

    public void setCheckBoxSelected(boolean selected) {
        this.checkBox.setSelected(selected);
    }

    private final ObjectProperty<StringConverter<T>> converter =
        new SimpleObjectProperty<>(this, "converter") {protected void invalidated() {updateShowLabel();}};

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    private final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallback =
        new SimpleObjectProperty<>(this, "selectedStateCallback");

    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }

    public final Callback<Integer, ObservableValue<Boolean>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            StringConverter<T> c = getConverter();

            if (showLabel) {
                setText(c.toString(item));
            }
            setGraphic(checkBox);

            if (booleanProperty instanceof BooleanProperty) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty)booleanProperty);
            }
            ObservableValue<?> obsValue = getSelectedProperty();
            if (obsValue instanceof BooleanProperty) {
                booleanProperty = (ObservableValue<Boolean>) obsValue;
                checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
            }

            checkBox.disableProperty().bind(Bindings.not(
                getTableView().editableProperty().and(
                    getTableColumn().editableProperty()).and(
                    editableProperty())
            ));
        }
    }

    private void updateShowLabel() {
        this.showLabel = converter != null;
        this.checkBox.setAlignment(showLabel ? Pos.CENTER_LEFT : Pos.CENTER);
    }

    private ObservableValue<?> getSelectedProperty() {
        return getSelectedStateCallback() != null ?
            getSelectedStateCallback().call(getIndex()) :
            getTableColumn().getCellObservableValue(getIndex());
    }
}