package com.tsel.home.project.booklibrary.javafx;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.service.ReferenceService.Entry;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReferenceWindowsTest extends UnitJavaFXTest {

    @Test
    void menusEditorsAndAboutWorkWithIsolatedCatalog(@TempDir Path directory) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            DesignFixture.initialize(directory);
            String motion = System.getProperty("booklibrary.reducedMotion");
            try {
                System.setProperty("booklibrary.reducedMotion", "true");
                DesignFixture.populate();
                getBean(AuthorRepository.class).save(new Author(null, "Свободный автор"));
                getBean(CycleRepository.class).save(new Cycle(null, "Свободный цикл", false, 0));
                getBean(GenreRepository.class).save(new Genre(null, "Свободный жанр"));
                getBean(PublisherRepository.class).save(new Publisher(null, "Свободный публицист"));
                Stage main = new Stage();
                new MainViewController().startScene(main);
                MenuBar bar = (MenuBar) main.getScene().lookup(".menu-bar");
                assertEquals(List.of("Файлы", "Правка", "Справка"), bar.getMenus().stream().map(Menu::getText).toList());
                assertTrue(bar.getMenus().get(2).getItems().get(0).isDisable());
                assertEquals(3, getBean(AuthorRepository.class).getAll().size(), "Startup must retain unused entries");

                for (int index = 0; index < 4; index++) {
                    bar.getMenus().get(1).getItems().get(index).fire();
                    Stage editor = childOf(main);
                    layout(editor);
                    assertControlsInside(editor.getScene());
                    assertTrue(editor.getScene().lookup("#saveReferenceButton").isDisabled());
                    if (index == 0) {
                        TextField search = (TextField) editor.getScene().lookup("#referenceSearch");
                        search.setText("Хроники");
                        assertTrue(editor.getScene().lookup("#deleteReferenceButton").isDisabled());
                        assertEquals("Связанных книг: 2", ((Label) editor.getScene().lookup("#referenceBookCount")).getText());
                        capture(editor, "reference-cycles");
                        TableView<?> properties = (TableView<?>) editor.getScene().lookup("#referenceProperties");
                        assertEquals(4, properties.getItems().size());
                        TextField identifier = (TextField) editor.getScene().lookup("#referenceId");
                        assertFalse(identifier.isEditable());
                        TextField total = (TextField) editor.getScene().lookup("#referenceBooksInCycle");
                        total.setText("-1");
                        assertNotEquals("-1", total.getText());
                        total.setText("8");
                        ((CheckBox) editor.getScene().lookup("#referenceEnded")).setSelected(false);
                        ((Button) editor.getScene().lookup("#saveReferenceButton")).fire();
                        assertEquals(8, getBean(CycleRepository.class).getAll().stream()
                            .filter(cycle -> cycle.getName().equals("Хроники Дюны")).findFirst().orElseThrow().getBooksInCycle());
                    } else if (index == 1) {
                        checkAuthorEditor(editor);
                    } else {
                        assertNotNull(editor.getScene().lookup("#referenceEntries"));
                    }
                    editor.hide();
                }

                bar.getMenus().get(2).getItems().get(1).fire();
                Stage about = childOf(main);
                layout(about);
                assertEquals("for Jane", ((Label) about.getScene().lookup("#dedication")).getText());
                capture(about, "about");
                about.close();
                System.setProperty("booklibrary.reducedMotion", "false");
                bar.getMenus().get(2).getItems().get(1).fire();
                childOf(main).close();
            } finally {
                for (Window window : List.copyOf(Window.getWindows())) {
                    window.hide();
                }
                DesignFixture.restore();
                if (motion == null) {
                    System.clearProperty("booklibrary.reducedMotion");
                } else {
                    System.setProperty("booklibrary.reducedMotion", motion);
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(50, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void checkAuthorEditor(Stage editor) throws Exception {
        Scene scene = editor.getScene();
        TextField search = (TextField) scene.lookup("#referenceSearch");
        search.setText("Фрэнк");
        assertEquals("Связанных книг: 6", ((Label) scene.lookup("#referenceBookCount")).getText());
        assertTrue(scene.lookup("#deleteReferenceButton").isDisabled());
        capture(editor, "reference-authors");
        TextField name = (TextField) scene.lookup("#referenceName");
        name.setText(" ");
        ((Button) scene.lookup("#saveReferenceButton")).fire();
        assertTrue(((Label) scene.lookup("#referenceStatus")).getText().contains("Введите"));
        assertEquals(" ", name.getText());
        ((Button) scene.lookup("#resetReferenceButton")).fire();
        name.setText("Фрэнк Герберт (правка)");
        checkLinkedBookNavigation(editor, name);
        respondToConfirmation(false);
        search.setText("Свободный автор");
        assertEquals("Фрэнк", search.getText(), "Canceling search must preserve the query and draft");
        assertEquals("Фрэнк Герберт (правка)", name.getText());
        respondToConfirmation(false);
        SageTheme.requestClose(editor);
        assertTrue(editor.isShowing());
        assertEquals("Фрэнк Герберт (правка)", name.getText());
        ((Button) scene.lookup("#saveReferenceButton")).fire();
        assertTrue(getBean(AuthorRepository.class).getByName("Фрэнк Герберт (правка)").isPresent());
        search.setText("Свободный автор");
        Button delete = (Button) scene.lookup("#deleteReferenceButton");
        assertFalse(delete.isDisabled());
        respondToConfirmation(true);
        delete.fire();
        assertTrue(getBean(AuthorRepository.class).getByName("Свободный автор").isEmpty());
        assertEquals(0, ((TableView<Entry>) scene.lookup("#referenceEntries")).getItems().size());
        search.setText("несуществующая запись");
        assertTrue(scene.lookup("#saveReferenceButton").isDisabled());
    }

    private void checkLinkedBookNavigation(Stage editor, TextField name) {
        layout(editor);
        Hyperlink link = editor.getScene().getRoot().lookupAll(".reference-book-link").stream()
            .map(Hyperlink.class::cast).filter(candidate -> "Дюна".equals(candidate.getText())).findFirst().orElseThrow();
        link.fire();
        Stage book = childOf(editor);
        assertEquals("Карточка книги", book.getTitle());
        assertEquals("Дюна", ((Label) book.getScene().lookup("#nameLabel")).getText());
        book.close();
        assertEquals("Фрэнк Герберт (правка)", name.getText(), "Opening a book must preserve the reference draft");
        assertFalse(editor.getScene().lookup("#saveReferenceButton").isDisabled());
        layout(editor);

        Hyperlink deletionLink = editor.getScene().getRoot().lookupAll(".reference-book-link").stream()
            .map(Hyperlink.class::cast).filter(candidate -> "Дюна".equals(candidate.getText())).findFirst().orElseThrow();
        deletionLink.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        Stage reopened = childOf(editor);
        respondToConfirmation(true);
        ((Button) reopened.getScene().lookup("#deleteBookButton")).fire();
        assertFalse(reopened.isShowing());
        assertEquals("Связанных книг: 5", ((Label) editor.getScene().lookup("#referenceBookCount")).getText());
        assertEquals("Фрэнк Герберт (правка)", name.getText());
        assertTrue(getBean(BookRepository.class).getAll().stream().noneMatch(value -> "Дюна".equals(value.getName())));
    }

    private void respondToConfirmation(boolean accept) {
        Platform.runLater(() -> {
            Stage dialog = (Stage) Window.getWindows().stream()
                .filter(window -> window.getScene().getRoot() instanceof DialogPane).findFirst().orElseThrow();
            DialogPane pane = (DialogPane) dialog.getScene().getRoot();
            ButtonType response = pane.getButtonTypes().stream()
                .filter(type -> type.getButtonData() == (accept ? ButtonBar.ButtonData.OK_DONE : ButtonBar.ButtonData.CANCEL_CLOSE))
                .findFirst().orElseThrow();
            ((Button) pane.lookupButton(response)).fire();
        });
    }

    private Stage childOf(Stage owner) {
        return (Stage) Window.getWindows().stream()
            .filter(window -> window instanceof Stage child && child.getOwner() == owner && child.isShowing())
            .findFirst().orElseThrow();
    }

    private void layout(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    private void assertControlsInside(Scene scene) {
        for (String id : List.of("referenceEntries", "referenceBooks", "referenceProperties", "referenceName", "referenceId", "deleteReferenceButton",
            "resetReferenceButton", "saveReferenceButton")) {
            Node node = scene.lookup("#" + id);
            javafx.geometry.Bounds bounds = node.localToScene(node.getBoundsInLocal());
            assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= scene.getWidth(), id);
            assertTrue(bounds.getMinY() >= 0 && bounds.getMaxY() <= scene.getHeight(), id);
        }
    }

    private void capture(Stage stage, String name) throws Exception {
        if (!Boolean.getBoolean("booklibrary.captureMenus")) {
            return;
        }
        layout(stage);
        WritableImage snapshot = stage.getScene().getRoot().snapshot(null, null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        Path path = Path.of("target", name + ".png");
        Files.createDirectories(path.getParent());
        ImageIO.write(image, "png", path.toFile());
    }
}
