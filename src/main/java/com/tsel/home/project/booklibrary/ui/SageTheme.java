package com.tsel.home.project.booklibrary.ui;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public final class SageTheme {
    private static javafx.scene.image.Image windowIcon;
    private SageTheme() {}

    public static javafx.scene.image.Image windowIcon() {
        if (windowIcon == null) {
            LineIcon icon = new LineIcon("book");
            icon.resize(20, 20);
            icon.layout();
            javafx.scene.SnapshotParameters parameters = new javafx.scene.SnapshotParameters();
            parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
            parameters.setTransform(javafx.scene.transform.Transform.scale(3.2, 3.2));
            windowIcon = icon.snapshot(parameters, null);
        }
        return windowIcon;
    }

    public static void apply(Scene scene) {
        scene.getStylesheets().add(SageTheme.class.getResource("/com/tsel/home/project/booklibrary/css/sage.css").toExternalForm());
    }

    public static Button iconButton(String icon, String name, Runnable action) {
        Button button = new Button();
        button.setGraphic(new LineIcon(icon));
        button.setAccessibleText(name);
        button.setTooltip(new Tooltip(name));
        button.getStyleClass().add("icon-button");
        button.setOnAction(e -> action.run());
        return button;
    }

    public static void install(Stage stage, Scene scene, String title, boolean resizable) {
        apply(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(title);
        HBox header = (HBox) scene.lookup("#windowHeader");
        if (header == null) return;
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 8, 0, 16));
        header.setSpacing(12);
        LineIcon logo = new LineIcon("book");
        logo.setScaleX(1.5);
        logo.setScaleY(1.5);
        Label label = new Label(title);
        label.getStyleClass().add(resizable ? "main-title" : "dialog-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().setAll(logo, label, spacer);
        if (resizable) {
            header.getChildren().add(iconButton("minimize", "Свернуть", () -> stage.setIconified(true)));
            Button maximize = iconButton("maximize", "Развернуть", () -> stage.setMaximized(!stage.isMaximized()));
            stage.maximizedProperty().addListener((o, a, b) -> {
                maximize.setGraphic(new LineIcon(b ? "restore" : "maximize"));
                maximize.setAccessibleText(b ? "Восстановить" : "Развернуть");
            });
            header.getChildren().add(maximize);
        }
        Button close = iconButton("close", "Закрыть", () -> requestClose(stage));
        close.getStyleClass().add("caption-close");
        header.getChildren().add(close);
        double[] drag = new double[2];
        header.setOnMousePressed(e -> { drag[0] = e.getScreenX() - stage.getX(); drag[1] = e.getScreenY() - stage.getY(); });
        header.setOnMouseDragged(e -> {
            if (isButton(e.getTarget()) || stage.isMaximized()) return;
            stage.setX(e.getScreenX() - drag[0]); stage.setY(e.getScreenY() - drag[1]);
        });
        header.setOnMouseClicked(e -> {
            if (resizable && e.getClickCount() == 2 && !isButton(e.getTarget())) stage.setMaximized(!stage.isMaximized());
        });
        ContextMenu systemMenu = new ContextMenu();
        if (resizable) {
            MenuItem restore = new MenuItem("Восстановить"); restore.setOnAction(e -> stage.setMaximized(false));
            MenuItem minimize = new MenuItem("Свернуть"); minimize.setOnAction(e -> stage.setIconified(true));
            MenuItem maximize = new MenuItem("Развернуть"); maximize.setOnAction(e -> stage.setMaximized(true));
            systemMenu.getItems().addAll(restore, minimize, maximize, new SeparatorMenuItem());
        }
        MenuItem quit = new MenuItem("Закрыть    Alt+F4"); quit.setOnAction(e -> requestClose(stage));
        systemMenu.getItems().add(quit);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isAltDown() && e.getCode() == KeyCode.SPACE) {
                systemMenu.show(header, javafx.geometry.Side.BOTTOM, 0, 0); e.consume();
            }
        });
        if (resizable) installResize(stage, scene);
    }

    private static boolean isButton(Object target) {
        for (Node n = target instanceof Node node ? node : null; n != null; n = n.getParent()) {
            if (n instanceof ButtonBase) return true;
        }
        return false;
    }

    public static void requestClose(Stage stage) {
        WindowEvent event = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
        stage.fireEvent(event);
        if (!event.isConsumed()) stage.close();
    }

    private static void installResize(Stage stage, Scene scene) {
        double[] start = new double[6];
        Cursor[] edge = {Cursor.DEFAULT};
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (stage.isMaximized()) { scene.setCursor(Cursor.DEFAULT); return; }
            boolean left = e.getSceneX() < 5, right = e.getSceneX() > scene.getWidth() - 5;
            boolean top = e.getSceneY() < 5, bottom = e.getSceneY() > scene.getHeight() - 5;
            edge[0] = top ? (left ? Cursor.NW_RESIZE : right ? Cursor.NE_RESIZE : Cursor.N_RESIZE)
                : bottom ? (left ? Cursor.SW_RESIZE : right ? Cursor.SE_RESIZE : Cursor.S_RESIZE)
                : left ? Cursor.W_RESIZE : right ? Cursor.E_RESIZE : Cursor.DEFAULT;
            scene.setCursor(edge[0]);
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            start[0] = e.getScreenX(); start[1] = e.getScreenY(); start[2] = stage.getX(); start[3] = stage.getY();
            start[4] = stage.getWidth(); start[5] = stage.getHeight();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (edge[0] == Cursor.DEFAULT || stage.isMaximized()) return;
            String direction = edge[0].toString().replace("_RESIZE", "");
            double dx = e.getScreenX() - start[0], dy = e.getScreenY() - start[1];
            if (direction.contains("W")) { double w = Math.max(stage.getMinWidth(), start[4] - dx); stage.setX(start[2] + start[4] - w); stage.setWidth(w); }
            if (direction.contains("E")) stage.setWidth(Math.max(stage.getMinWidth(), start[4] + dx));
            if (direction.contains("N")) { double h = Math.max(stage.getMinHeight(), start[5] - dy); stage.setY(start[3] + start[5] - h); stage.setHeight(h); }
            if (direction.contains("S")) stage.setHeight(Math.max(stage.getMinHeight(), start[5] + dy));
            e.consume();
        });
    }
}
