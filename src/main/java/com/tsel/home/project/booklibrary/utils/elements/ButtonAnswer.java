package com.tsel.home.project.booklibrary.utils.elements;

import javafx.scene.control.ButtonType;

public final class ButtonAnswer {

    private static final String OK = "OK";

    private final ButtonType buttonType;

    public ButtonAnswer(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public boolean isOkAnswer() {
        return buttonType != null
            && OK.equals(buttonType.getText());
    }
}
