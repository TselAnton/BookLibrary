package com.tsel.home.project.booklibrary.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

class WindowPlacementTest {

    @Test
    void testSelectScreenWithLargestWindowArea() {
        List<Rectangle2D> screens = List.of(
            new Rectangle2D(0, 0, 1920, 1080),
            new Rectangle2D(1920, 0, 1920, 1080)
        );

        assertEquals(1, WindowPlacement.findScreenIndex(new Rectangle2D(1800, 100, 1000, 700), screens));
        assertEquals(0, WindowPlacement.findScreenIndex(new Rectangle2D(1100, 100, 1000, 700), screens));
    }

    @Test
    void testSelectScreenAfterMovingMainWindow() {
        List<Rectangle2D> screens = List.of(
            new Rectangle2D(0, 0, 1920, 1080),
            new Rectangle2D(-1600, -900, 1600, 900)
        );

        assertEquals(0, WindowPlacement.findScreenIndex(new Rectangle2D(100, 100, 1000, 700), screens));
        assertEquals(1, WindowPlacement.findScreenIndex(new Rectangle2D(-1500, -850, 1000, 700), screens));
    }

    @Test
    void testCenterRelativeToMainWindowOnSecondaryScreen() {
        Point2D position = WindowPlacement.calculatePosition(
            new Rectangle2D(2100, 100, 1200, 800), 484, 675, new Rectangle2D(1920, 0, 1920, 1040)
        );

        assertEquals(new Point2D(2458, 162.5), position);
    }

    @Test
    void testKeepWindowInsideVisualBoundsNearTaskbar() {
        Point2D position = WindowPlacement.calculatePosition(
            new Rectangle2D(3600, 900, 200, 150), 812, 600, new Rectangle2D(1920, 0, 1920, 1040)
        );

        assertEquals(new Point2D(3028, 440), position);
    }

    @Test
    void testKeepWindowOnScreenWithNegativeCoordinates() {
        Point2D position = WindowPlacement.calculatePosition(
            new Rectangle2D(-1700, -1000, 1000, 700), 484, 675, new Rectangle2D(-1600, -900, 1600, 860)
        );

        assertEquals(new Point2D(-1442, -900), position);
    }

    @Test
    void testKeepTopOfOversizedWindowVisible() {
        Point2D position = WindowPlacement.calculatePosition(
            new Rectangle2D(100, 100, 800, 500), 812, 675, new Rectangle2D(0, 40, 800, 560)
        );

        assertEquals(new Point2D(0, 40), position);
    }
}
