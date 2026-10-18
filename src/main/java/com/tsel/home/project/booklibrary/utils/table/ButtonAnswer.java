package com.tsel.home.project.booklibrary.utils.table;

import javafx.scene.control.ButtonType;

public final class ButtonAnswer {

    private final ButtonType buttonType;

    public ButtonAnswer(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public boolean isOkAnswer() {
        return buttonType != null
            && buttonType.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
    }
}
