package com.tsel.home.project.booklibrary.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CatalogActionsTest extends UnitJavaFXTest {

    @Test
    void testPriceAndRandomSelectionRespectVisibleBooks(@TempDir Path directory) throws Exception {
        DesignFixture.initialize(directory);
        String reducedMotion = System.getProperty("booklibrary.reducedMotion");
        System.clearProperty("booklibrary.reducedMotion");
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage main = new Stage();
            try {
                DesignFixture.populate();
                new MainViewController().startScene(main);
                Scene scene = main.getScene();
                TableView<?> table = (TableView<?>) scene.lookup("#bookTableView");
                Label status = (Label) scene.lookup("#resultStatus");
                assertEquals("Найдено: 7 из 7", status.getText());
                table.getSelectionModel().select(0);
                assertEquals("Найдено: 7 из 7", status.getText());
                assertPriceDialog(main, "Стоимость 7 книг: 6\u00a0262 ₽");

                TextField search = (TextField) scene.lookup("#searchQueryField");
                search.setText("Марсианин");
                assertEquals(1, table.getItems().size());
                assertEquals("Найдено: 1 из 7", status.getText());
                assertPriceDialog(main, "Стоимость 1 книги: 1\u00a0252 ₽");
                Button random = (Button) scene.lookup("#randomBookButton");
                Node overlay = scene.lookup("#randomBookAnimation");
                random.fire();
                assertTrue(overlay.isVisible());
                assertTrue(overlay.isManaged());
                assertEquals(0, bookWindows(main).size());
                random.fire();
                search.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.N, false, true, false, false));
                settle(300);
                assertTrue(overlay.isVisible());
                assertEquals(0, bookWindows(main).size());
                Node dice = scene.lookup(".random-book-dice");
                assertTrue(dice.getTranslateY() < 0);
                assertEquals(0, dice.getRotate());
                assertEquals(3, scene.getRoot().lookupAll(".random-book-face").size());
                assertEquals(9, scene.getRoot().lookupAll(".random-book-pip").size());
                assertEquals(Color.WHITE, ((Label) scene.lookup(".random-book-caption")).getTextFill());
                capture(scene);
                settle(650);
                assertFalse(overlay.isVisible());
                assertFalse(overlay.isManaged());
                assertEquals(1, bookWindows(main).size());
                Stage details = bookWindows(main).get(0);
                assertTrue(details.getScene().getRoot().lookupAll(".label").stream()
                    .map(Label.class::cast).anyMatch(label -> "Марсианин".equals(label.getText())));
                assertFalse(((BookDTO) table.getItems().get(0)).getRead().isSelected());
                details.hide();

                random.fire();
                assertTrue(overlay.isVisible());
                main.hide();
                assertFalse(overlay.isVisible());
                settle(1000);
                assertEquals(0, bookWindows(main).size(), "Closing the main window must cancel the pending book");
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
            return null;
        });
        try {
            Platform.runLater(task);
            task.get(30, TimeUnit.SECONDS);
        } finally {
            if (reducedMotion == null) {
                System.clearProperty("booklibrary.reducedMotion");
            } else {
                System.setProperty("booklibrary.reducedMotion", reducedMotion);
            }
            DesignFixture.restore();
        }
    }

    private static void assertPriceDialog(Stage main, String expected) throws Exception {
        FutureTask<Void> inspect = new FutureTask<>(() -> {
            Stage dialog = (Stage) Window.getWindows().stream()
                .filter(window -> window instanceof Stage stage && "Стоимость книг".equals(stage.getTitle()))
                .findFirst().orElseThrow();
            try {
                assertTrue(dialog.getScene().getRoot().lookupAll(".label").stream()
                    .map(Label.class::cast).anyMatch(label -> expected.equals(label.getText())));
            } finally {
                dialog.hide();
            }
            return null;
        });
        Platform.runLater(inspect);
        ((Button) main.getScene().lookup("#priceButton")).fire();
        inspect.get(5, TimeUnit.SECONDS);
    }

    private static List<Stage> bookWindows(Stage owner) {
        return Window.getWindows().stream()
            .filter(window -> window instanceof Stage stage && stage.getOwner() == owner
                && "Карточка книги".equals(stage.getTitle()))
            .map(Stage.class::cast)
            .toList();
    }

    private static void settle(double milliseconds) {
        Object key = new Object();
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.exitNestedEventLoop(key, null));
        pause.play();
        Platform.enterNestedEventLoop(key);
    }

    private static void capture(Scene scene) throws Exception {
        if (!Boolean.getBoolean("booklibrary.captureCatalogActions")) {
            return;
        }
        WritableImage snapshot = scene.getRoot().snapshot(null, null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        ImageIO.write(image, "png", Path.of("target", "random-book-animation.png").toFile());
    }
}
