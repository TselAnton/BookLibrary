package com.tsel.home.project.booklibrary.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public final class AboutWindow {

    private AboutWindow() {}

    public static Stage show(Window owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        HBox header = new HBox();
        header.setId("windowHeader");
        header.getStyleClass().add("window-header");
        header.setMinHeight(48);
        header.setPrefHeight(48);
        Label title = new Label("BookLibrary");
        title.getStyleClass().add("about-name");
        Label description = new Label("Личная библиотека любимых книг");
        description.getStyleClass().add("caption");
        Label author = new Label("Автор — Anton Tsel");
        Label dedication = new Label("for Jane");
        dedication.setId("dedication");
        String handwriting = Font.getFamilies().contains("Segoe Script") ? "Segoe Script" : "cursive";
        dedication.setFont(Font.font(handwriting, 38));
        dedication.getStyleClass().add("about-dedication");
        HBox hearts = new HBox(10, heart(0.8), heart(1.1), heart(0.8));
        hearts.setAlignment(Pos.CENTER);
        VBox content = new VBox(14, title, description, author, dedication, hearts);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 20, 20, 20));
        Button close = new Button("Закрыть");
        close.setCancelButton(true);
        close.setDefaultButton(true);
        close.setOnAction(event -> stage.close());
        HBox footer = new HBox(close);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("dialog-footer");
        VBox window = new VBox(header, content, footer);
        window.setPrefSize(440, 398);
        VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);
        Pane particles = new Pane();
        particles.setMouseTransparent(true);
        StackPane root = new StackPane(window, particles);
        root.setPrefSize(440, 398);
        Scene scene = new Scene(root);
        SageTheme.install(stage, scene, "О программе", false);
        scene.getStylesheets().add(AboutWindow.class.getResource("/com/tsel/home/project/booklibrary/css/library-menu.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(SageTheme.windowIcon());
        Timeline animation = new Timeline();
        if (!Boolean.getBoolean("booklibrary.reducedMotion")) {
            for (int i = 0; i < 22; i++) {
                SVGPath particle = heart(0.5 + (i % 4) * 0.2);
                particle.setLayoutX(220);
                particle.setLayoutY(290);
                double angle = Math.PI * 2 * i / 22;
                double distance = 65 + (i % 5) * 24;
                particles.getChildren().add(particle);
                animation.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(particle.opacityProperty(), 0)),
                    new KeyFrame(Duration.millis(120 + i * 12), new KeyValue(particle.opacityProperty(), 0.85)),
                    new KeyFrame(Duration.millis(1500 + i * 15),
                        new KeyValue(particle.translateXProperty(), Math.cos(angle) * distance, Interpolator.EASE_OUT),
                        new KeyValue(particle.translateYProperty(), Math.sin(angle) * distance - 60, Interpolator.EASE_OUT),
                        new KeyValue(particle.rotateProperty(), (i % 2 == 0 ? 1 : -1) * 35),
                        new KeyValue(particle.opacityProperty(), 0))
                );
            }
        }
        animation.setOnFinished(event -> particles.getChildren().clear());
        stage.setOnHidden(event -> {
            animation.stop();
            particles.getChildren().clear();
        });
        WindowPlacement.positionRelativeToMainWindow(stage);
        stage.show();
        animation.playFromStart();
        return stage;
    }

    private static SVGPath heart(double scale) {
        SVGPath heart = new SVGPath();
        heart.setContent("M 0 5 C 0 -2 10 -2 10 5 C 10 -2 20 -2 20 5 C 20 12 10 19 10 19 C 10 19 0 12 0 5 Z");
        heart.setFill(Color.web("#B45B76"));
        heart.setScaleX(scale);
        heart.setScaleY(scale);
        return heart;
    }
}
