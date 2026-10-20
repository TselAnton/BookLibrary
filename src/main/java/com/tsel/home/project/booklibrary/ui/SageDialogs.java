package com.tsel.home.project.booklibrary.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class SageDialogs {

    private SageDialogs() {}

    public static Alert create(
        Window owner,
        Alert.AlertType type,
        String title,
        String message,
        String details,
        ButtonType... buttons
    ) {
        Alert dialog = new Alert(type);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(null);
        dialog.setGraphic(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("message-dialog");
        pane.setPrefWidth(520);
        pane.setMinWidth(400);
        HBox header = new HBox();
        header.setId("windowHeader");
        header.getStyleClass().add("window-header");
        header.setMinHeight(56);
        header.setPrefHeight(56);
        pane.setHeader(header);

        Label heading = new Label(message);
        heading.setWrapText(true);
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.getStyleClass().add("message-heading");
        Label explanation = new Label(details);
        explanation.setWrapText(true);
        explanation.setMaxWidth(Double.MAX_VALUE);
        explanation.setVisible(details != null && !details.isBlank());
        explanation.managedProperty().bind(explanation.visibleProperty());
        VBox content = new VBox(12, heading, explanation);
        content.setAlignment(Pos.TOP_LEFT);
        content.getStyleClass().add("message-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("message-scroll");
        pane.setContent(scroll);
        pane.getButtonTypes().setAll(buttons);
        ButtonType cancel = null;
        for (ButtonType buttonType : buttons) {
            Button button = (Button) pane.lookupButton(buttonType);
            boolean isCancel = buttonType.getButtonData().isCancelButton();
            button.setDefaultButton(buttons.length == 1 || isCancel);
            if (isCancel) {
                cancel = buttonType;
            } else {
                button.getStyleClass().add("primary");
            }
        }
        Stage stage = (Stage) pane.getScene().getWindow();
        SageTheme.install(stage, pane.getScene(), title, false);
        pane.applyCss();
        double height = heading.prefHeight(472) + (explanation.isManaged() ? explanation.prefHeight(472) + 12 : 0) + 48;
        scroll.setPrefViewportHeight(Math.min(300, Math.max(96, height)));
        stage.getIcons().setAll(SageTheme.windowIcon());
        ButtonType focus = cancel == null ? buttons[0] : cancel;
        dialog.setOnShown(event -> Platform.runLater(() -> pane.lookupButton(focus).requestFocus()));
        return dialog;
    }

    public static boolean confirm(Window owner, String title, String message, String action) {
        ButtonType accept = new ButtonType(action, ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonData.CANCEL_CLOSE);
        return create(owner, Alert.AlertType.CONFIRMATION, title, message, "", accept, cancel)
            .showAndWait().orElse(cancel) == accept;
    }
}
