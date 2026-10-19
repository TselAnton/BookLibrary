package com.tsel.home.project.booklibrary.ui;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class WindowPlacement {

    private WindowPlacement() {}

    /**
     * Разместить окно относительно текущего положения главного окна в цепочке владельцев.
     * @param stage Окно с установленными сценой и владельцем
     */
    public static void positionRelativeToMainWindow(Stage stage) {
        Window mainWindow = stage.getOwner();
        while (mainWindow instanceof Stage ownerStage && ownerStage.getOwner() != null) {
            mainWindow = ownerStage.getOwner();
        }

        Rectangle2D mainBounds = new Rectangle2D(
            mainWindow.getX(), mainWindow.getY(), mainWindow.getWidth(), mainWindow.getHeight()
        );
        List<Screen> screens = Screen.getScreens();
        int screenIndex = findScreenIndex(mainBounds, screens.stream().map(Screen::getBounds).toList());
        Rectangle2D visualBounds = screens.get(screenIndex).getVisualBounds();

        Parent root = stage.getScene().getRoot();
        root.applyCss();
        double width = root.prefWidth(-1);
        double height = root.prefHeight(width);
        Point2D position = calculatePosition(mainBounds, width, height, visualBounds);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(position.getX());
        stage.setY(position.getY());
    }

    static int findScreenIndex(Rectangle2D windowBounds, List<Rectangle2D> screenBounds) {
        int selectedIndex = 0;
        double largestArea = 0;
        for (int i = 0; i < screenBounds.size(); i++) {
            Rectangle2D bounds = screenBounds.get(i);
            double width = Math.max(0, Math.min(windowBounds.getMaxX(), bounds.getMaxX())
                - Math.max(windowBounds.getMinX(), bounds.getMinX()));
            double height = Math.max(0, Math.min(windowBounds.getMaxY(), bounds.getMaxY())
                - Math.max(windowBounds.getMinY(), bounds.getMinY()));
            double area = width * height;
            if (area > largestArea) {
                selectedIndex = i;
                largestArea = area;
            }
        }
        return selectedIndex;
    }

    static Point2D calculatePosition(Rectangle2D mainBounds, double width, double height, Rectangle2D visualBounds) {
        double x = mainBounds.getMinX() + (mainBounds.getWidth() - width) / 2;
        double y = mainBounds.getMinY() + (mainBounds.getHeight() - height) / 2;
        return new Point2D(
            Math.max(visualBounds.getMinX(), Math.min(x, visualBounds.getMaxX() - width)),
            Math.max(visualBounds.getMinY(), Math.min(y, visualBounds.getMaxY() - height))
        );
    }
}
