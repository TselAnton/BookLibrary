package com.tsel.home.project.booklibrary.utils.table;

import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioBookSiteNameEditableColumn extends TableCell<AudioBookSiteDTO, String> {

    private static final Logger log = LogManager.getLogger(AudioBookSiteNameEditableColumn.class);

    private TextField inputTextColumn;

    @Override
    public void startEdit() {
        if (!this.isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(inputTextColumn);
            inputTextColumn.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(item);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (inputTextColumn != null) {
                    inputTextColumn.setText(getString());
                }
                setText(null);
                setGraphic(inputTextColumn);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        inputTextColumn = new TextField(getString());
        inputTextColumn.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        inputTextColumn.setOnAction(event -> commitEdit(inputTextColumn.getText()));
        inputTextColumn.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                log.info("Committing editable text: {}", inputTextColumn.getText());
                commitEdit(inputTextColumn.getText());
            }
        });
    }

    private String getString() {
        return ofNullable(this.getItem()).orElse("");
    }
}
