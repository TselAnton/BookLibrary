package com.tsel.home.project.booklibrary.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookWindowPlacementTest extends UnitJavaFXTest {

    @Test
    void testBookWindowsFollowCurrentMainWindow(@TempDir Path directory) throws Exception {
        DesignFixture.initialize(directory);
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                DesignFixture.populate();
                Stage main = new Stage();
                new MainViewController().startScene(main);
                TableView<?> table = (TableView<?>) main.getScene().lookup("#bookTableView");
                table.getSelectionModel().selectFirst();

                for (Screen screen : Screen.getScreens()) {
                    Rectangle2D bounds = screen.getVisualBounds();
                    main.setWidth(Math.max(main.getMinWidth(), Math.min(1000, bounds.getWidth())));
                    main.setHeight(Math.max(main.getMinHeight(), Math.min(800, bounds.getHeight())));
                    main.setX(bounds.getMinX() + 40);
                    main.setY(bounds.getMinY() + 40);

                    openSelectedBook(table);
                    Stage details = findStage("Карточка книги");
                    assertSame(main, details.getOwner());
                    assertPlacement(details, main, bounds);

                    // Карточка остаётся в старом месте после перемещения главного окна.
                    details.setX(bounds.getMinX() - details.getWidth());
                    details.setY(bounds.getMinY() - details.getHeight());
                    main.setX(bounds.getMinX() + 100);
                    main.setY(bounds.getMinY() + 80);

                    checkModalWindow(
                        () -> ((Button) main.getScene().lookup("#addBookButton")).fire(),
                        "Добавить книгу", main, main, bounds
                    );
                    checkModalWindow(
                        () -> ((Button) details.getScene().lookup("#editBookButton")).fire(),
                        "Изменить книгу", details, main, bounds
                    );

                    openSelectedBook(table);
                    assertPlacement(findStage("Карточка книги"), main, bounds);
                }
            } finally {
                for (Window window : List.copyOf(Window.getWindows())) {
                    window.hide();
                }
                DesignFixture.restore();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(40, TimeUnit.SECONDS);
    }

    private static void openSelectedBook(TableView<?> table) {
        table.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
    }

    private static Stage findStage(String title) {
        return Window.getWindows().stream()
            .filter(window -> window instanceof Stage stage && title.equals(stage.getTitle()))
            .map(window -> (Stage) window)
            .findFirst()
            .orElseThrow();
    }

    private static void checkModalWindow(
        Runnable openWindow,
        String title,
        Stage owner,
        Stage main,
        Rectangle2D bounds
    ) throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Stage modal = findStage(title);
            try {
                assertSame(owner, modal.getOwner());
                assertPlacement(modal, main, bounds);
            } finally {
                modal.hide();
            }
            return null;
        });
        Platform.runLater(check);
        openWindow.run();
        check.get(5, TimeUnit.SECONDS);
    }

    private static void assertPlacement(Stage stage, Stage main, Rectangle2D bounds) {
        double x = main.getX() + (main.getWidth() - stage.getWidth()) / 2;
        double y = main.getY() + (main.getHeight() - stage.getHeight()) / 2;
        assertEquals(Math.max(bounds.getMinX(), Math.min(x, bounds.getMaxX() - stage.getWidth())), stage.getX(), 1,
            stage.getTitle() + ": main=" + main.getX() + "," + main.getWidth()
                + ", stage=" + stage.getX() + "," + stage.getWidth() + ", scene=" + stage.getScene().getWidth()
                + ", screen=" + bounds);
        assertEquals(Math.max(bounds.getMinY(), Math.min(y, bounds.getMaxY() - stage.getHeight())), stage.getY(), 1);
    }
}
