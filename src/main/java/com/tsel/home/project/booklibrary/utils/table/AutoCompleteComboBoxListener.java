package com.tsel.home.project.booklibrary.utils.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This solution taken from https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
 * @param <T> CheckBox values type
 */
public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {

    private final ComboBox<T> comboBox;
    private final ObservableList<T> data;
    private boolean moveCaretToPos = false;
    private int caretPos;

    public AutoCompleteComboBoxListener(final ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        data = comboBox.getItems();

        this.comboBox.setEditable(true);
        this.comboBox.setOnKeyPressed(t -> comboBox.hide());
        this.comboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
    }

    @Override
    public void handle(KeyEvent event) {
        switch (event.getCode()) {
            case UP -> {
                caretPos = -1;
                moveCaret(comboBox.getEditor().getText().length());
            }
            case DOWN -> {
                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
                caretPos = -1;
                moveCaret(comboBox.getEditor().getText().length());
            }
            case BACK_SPACE, DELETE -> {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            }
            default -> {
                // NOT NEEDED
            }
        }

        if (event.getCode() == KeyCode.RIGHT
                || event.getCode() == KeyCode.LEFT
                || event.isControlDown()
                || event.getCode() == KeyCode.HOME
                || event.getCode() == KeyCode.END
                || event.getCode() == KeyCode.TAB) {
            return;
        }

        ObservableList<T> list = FXCollections.observableArrayList();
        for (T datum : data) {
            String comboBoxText = AutoCompleteComboBoxListener.this.comboBox
                    .getEditor()
                    .getText()
                    .toLowerCase();

            if (datum.toString().toLowerCase().startsWith(comboBoxText)) {
                list.add(datum);
            }
        }

        String t = comboBox.getEditor().getText();

        comboBox.setItems(list);
        comboBox.getEditor().setText(t);

        if (!moveCaretToPos) {
            caretPos = -1;
        }

        moveCaret(t.length());
        if (!list.isEmpty()) {
            comboBox.show();
        }
    }

    private void moveCaret(int textLength) {
        if (caretPos == -1) {
            comboBox.getEditor().positionCaret(textLength);
        } else {
            comboBox.getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
    }

}