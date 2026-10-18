package com.tsel.home.project.booklibrary.utils.table;

import java.util.Locale;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Filters lookup suggestions after text edits without interfering with native selection shortcuts. */
public final class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {
    private final ComboBox<T> comboBox;
    private final ObservableList<T> data;
    private String lastFilteredText;

    public static <T> void wrapComboBox(ComboBox<T> comboBox) {
        new AutoCompleteComboBoxListener<>(comboBox);
    }

    private AutoCompleteComboBoxListener(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        data = FXCollections.observableArrayList(comboBox.getItems());
        comboBox.setEditable(true);
        lastFilteredText = comboBox.getEditor().getText();
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (isTextEdit(event) && !event.isShortcutDown()) comboBox.hide();
        });
        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, this);
    }

    private static boolean isTextEdit(KeyEvent event) {
        KeyCode code = event.getCode();
        return !code.isModifierKey() && !code.isNavigationKey() && !code.isFunctionKey()
            && code != KeyCode.ESCAPE && code != KeyCode.ENTER && code != KeyCode.TAB
            && (!event.isShortcutDown() || code == KeyCode.V || code == KeyCode.X);
    }

    @Override public void handle(KeyEvent event) {
        if (!isTextEdit(event)) return;
        String text = comboBox.getEditor().getText();
        if (Objects.equals(text, lastFilteredText)) return;
        lastFilteredText = text;
        int anchor = comboBox.getEditor().getAnchor();
        int caret = comboBox.getEditor().getCaretPosition();
        String prefix = text.toLowerCase(Locale.ROOT);
        ObservableList<T> matches = FXCollections.observableArrayList();
        for (T item : data) {
            if (item.toString().toLowerCase(Locale.ROOT).startsWith(prefix)) matches.add(item);
        }
        comboBox.setItems(matches);
        comboBox.getEditor().setText(text);
        if (matches.isEmpty()) comboBox.hide();
        else if (comboBox.getScene() != null && comboBox.getScene().getWindow().isShowing()) comboBox.show();
        comboBox.getEditor().selectRange(anchor, caret);
    }
}
