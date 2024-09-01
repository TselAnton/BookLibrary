package com.tsel.home.project.booklibrary.utils.table;

import java.util.UUID;
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
 * CheckBox колонка с отслеживанием отслеживать евентов самих CheckBox
 */
@SuppressWarnings("unchecked")
public abstract class CustomCheckBoxTableCell<S> extends TableCell<S, CheckBox> {

    private final CheckBox checkBox;
    private UUID id;

    private boolean showLabel;

    private ObservableValue<Boolean> booleanProperty;

    protected CustomCheckBoxTableCell() {
        this.getStyleClass().add("check-box");
        this.setStyle("-fx-alignment: CENTER");

        this.checkBox = new CheckBox();
        this.checkBox.setId("selectAudioBookSiteCheckBox");
        this.checkBox.setOnAction(this::handleActionEvent);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
    }

    protected void handleActionEvent(ActionEvent event) {}

    public void setCheckBoxId(UUID id) {
        this.id = id;
    }

    public UUID getCheckBoxId() {
        return this.id;
    }

    public boolean isCheckBoxSelected() {
        return this.checkBox.isSelected();
    }

    public void setCheckBoxSelected(boolean selected) {
        this.checkBox.setSelected(selected);
    }

    private final ObjectProperty<StringConverter<CheckBox>> converter =
        new SimpleObjectProperty<>(this, "converter") {protected void invalidated() {updateShowLabel();}};

    public final ObjectProperty<StringConverter<CheckBox>> converterProperty() {
        return converter;
    }

    public final StringConverter<CheckBox> getConverter() {
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
    public void updateItem(CheckBox item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            UUID checkBoxId = UUID.fromString(item.getId());
            this.setCheckBoxId(checkBoxId);
            this.setCheckBoxSelected(setSelectedOnInit(checkBoxId));
        }

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            StringConverter<CheckBox> c = getConverter();

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

    protected abstract boolean setSelectedOnInit(UUID checkBoxId);

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