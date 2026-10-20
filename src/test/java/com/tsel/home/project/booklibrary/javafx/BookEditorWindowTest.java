package com.tsel.home.project.booklibrary.javafx;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.ui.BookDateEditor;
import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookEditorWindowTest extends UnitJavaFXTest {

    @Test
    @SuppressWarnings("unchecked")
    void editDatesSearchAndOpenStandardEditor(@TempDir Path directory) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            DesignFixture.initialize(directory);
            try {
                DesignFixture.populate();
                Book legacy = getBean(BookRepository.class).getAll().stream().filter(book -> book.getName().equals("Дюна"))
                    .findFirst().orElseThrow();
                legacy.setRead(null);
                legacy.setAutograph(null);
                getBean(BookRepository.class).save(legacy);
                Stage main = new Stage();
                new MainViewController().startScene(main);
                MenuBar menu = (MenuBar) main.getScene().lookup(".menu-bar");
                menu.getMenus().get(1).getItems().stream().filter(item -> item.getText().equals("Правка книг"))
                    .findFirst().orElseThrow().fire();
                Stage window = child(main);
                window.getScene().getRoot().applyCss();
                window.getScene().getRoot().layout();
                TableView<Book> books = (TableView<Book>) window.getScene().lookup("#bookPropertiesList");
                TextField search = (TextField) window.getScene().lookup("#bookPropertiesSearch");
                search.setText("Герберт");
                assertEquals(6, books.getItems().size());
                books.getSelectionModel().select(books.getItems().stream().filter(book -> book.getPrice() != null).findFirst().orElseThrow());
                Book original = books.getSelectionModel().getSelectedItem();
                assertFalse(((TextField) window.getScene().lookup("#bookPropertyauthorId")).isEditable());
                assertTrue(window.getScene().lookup("#saveBookProperties").isDisabled());
                DatePicker created = (DatePicker) window.getScene().lookup("#bookPropertycreatedAtDate");
                created.setValue(LocalDate.of(2020, 1, 2));
                ((TextField) window.getScene().lookup("#bookPropertycreatedAtTime")).setText("10:11:12.123");
                ((Button) window.getScene().lookup("#saveBookProperties")).fire();
                long expected = LocalDate.of(2020, 1, 2).atTime(LocalTime.of(10, 11, 12, 123000000))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                Book saved = getBean(BookRepository.class).getById(original.getId());
                assertEquals(expected, saved.getCreatedAt());
                assertEquals(original.getUpdatedAt(), saved.getUpdatedAt());
                assertEquals(original.getAuthorId(), saved.getAuthorId());
                assertTrue(window.getScene().lookup("#saveBookProperties").isDisabled());
                ((Hyperlink) window.getScene().lookup("#openBookEditor")).fire();
                Stage legacyEditor = child(window);
                CheckBox legacyRead = (CheckBox) legacyEditor.getScene().lookup("#readCheckBox");
                assertFalse(legacyRead.isAllowIndeterminate());
                assertFalse(legacyRead.isIndeterminate());
                assertFalse(legacyRead.isSelected());
                ((Button) legacyEditor.getScene().lookup("#saveBookButton")).fire();
                assertFalse(legacyEditor.isShowing());
                assertEquals(saved, getBean(BookRepository.class).getById(saved.getId()));
                ((TextField) window.getScene().lookup("#bookPropertypages")).setText("-1");
                ((Button) window.getScene().lookup("#saveBookProperties")).fire();
                assertEquals(saved.getPages(), getBean(BookRepository.class).getById(saved.getId()).getPages());
                created.setValue(LocalDate.of(2019, 3, 4));
                ((Button) window.getScene().lookup("#resetBookProperties")).fire();
                assertEquals(LocalDate.of(2020, 1, 2), created.getValue());
                assertEquals(saved, getBean(BookRepository.class).getById(saved.getId()));
                window.getScene().getRoot().applyCss();
                window.getScene().getRoot().layout();
                assertTrue(books.getColumns().get(0).getWidth() >= 150);
                capture(window);
                CheckBox read = (CheckBox) window.getScene().lookup("#bookPropertyread");
                assertFalse(read.isAllowIndeterminate());
                BookDateEditor readDate = (BookDateEditor) window.getScene().lookup("#bookPropertyreadUpdatedAt");
                read.setSelected(false);
                assertTrue(readDate.isDisabled());
                assertTrue(readDate.getChildren().stream().allMatch(javafx.scene.Node::isDisabled));
                read.setSelected(true);
                assertFalse(readDate.isDisabled());
                javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) read.getParent();
                assertEquals(0, javafx.scene.layout.GridPane.getRowIndex(window.getScene().lookup("#bookPropertyid")));
                assertEquals(javafx.scene.layout.GridPane.getRowIndex(read) + 1, javafx.scene.layout.GridPane.getRowIndex(readDate));
                assertEquals(grid.getRowCount() - 2, javafx.scene.layout.GridPane.getRowIndex(created.getParent()));
                assertEquals(grid.getRowCount() - 1, javafx.scene.layout.GridPane.getRowIndex(window.getScene().lookup("#bookPropertyupdatedAt")));
                assertEquals("Открыть на редактирование", ((Hyperlink) window.getScene().lookup("#openBookEditor")).getText());
                for (String field : List.of("updatedAt", "readUpdatedAt")) {
                    ((DatePicker) window.getScene().lookup("#bookProperty" + field + "Date")).setValue(LocalDate.of(2020, 1, 2));
                    ((TextField) window.getScene().lookup("#bookProperty" + field + "Time")).setText("10:11:12.123");
                }
                ((Button) window.getScene().lookup("#saveBookProperties")).fire();
                Book corrected = getBean(BookRepository.class).getById(original.getId());
                assertEquals(expected, corrected.getUpdatedAt());
                assertEquals(expected, corrected.getReadUpdatedAt());
                TextField name = (TextField) window.getScene().lookup("#bookPropertyname");
                name.setText("Несохранённое название");
                respondToConfirmation(false);
                books.getSelectionModel().select((books.getSelectionModel().getSelectedIndex() + 1) % books.getItems().size());
                Object pendingSelection = new Object();
                Platform.runLater(() -> Platform.exitNestedEventLoop(pendingSelection, null));
                Platform.enterNestedEventLoop(pendingSelection);
                assertEquals(original.getId(), books.getSelectionModel().getSelectedItem().getId());
                assertEquals("Несохранённое название", name.getText());
                respondToConfirmation(false);
                search.setText("другой поиск");
                assertEquals("Герберт", search.getText());
                respondToConfirmation(false);
                ((Hyperlink) window.getScene().lookup("#openBookEditor")).fire();
                assertEquals("Несохранённое название", name.getText());
                assertTrue(Window.getWindows().stream().noneMatch(value -> value instanceof Stage stage && stage.getOwner() == window));
                assertEquals(corrected, getBean(BookRepository.class).getById(original.getId()));
                respondToConfirmation(false);
                com.tsel.home.project.booklibrary.ui.SageTheme.requestClose(window);
                assertTrue(window.isShowing());
                assertEquals("Несохранённое название", name.getText());
                respondToConfirmation(true);
                ((Hyperlink) window.getScene().lookup("#openBookEditor")).fire();
                Stage standard = child(window);
                assertNotNull(standard.getScene().lookup("#saveBookButton"));
                assertEquals(corrected.getName(), ((TextField) standard.getScene().lookup("#nameTextFieldInput")).getText());
                assertFalse(((CheckBox) standard.getScene().lookup("#readCheckBox")).isAllowIndeterminate());
                assertTrue(((CheckBox) standard.getScene().lookup("#readCheckBox")).isSelected());
                ((Button) standard.getScene().lookup("#saveBookButton")).fire();
                assertFalse(standard.isShowing(), ((javafx.scene.control.Label) standard.getScene().lookup("#validationStatus")).getText());
                assertEquals(corrected, getBean(BookRepository.class).getById(original.getId()));
                ((Hyperlink) window.getScene().lookup("#openBookEditor")).fire();
                standard = child(window);
                ((TextField) standard.getScene().lookup("#nameTextFieldInput")).setText("Дюна после правки");
                ((Button) standard.getScene().lookup("#saveBookButton")).fire();
                Book renamed = getBean(BookRepository.class).getById(original.getId());
                assertEquals("Дюна после правки", renamed.getName());
                assertEquals(expected, renamed.getCreatedAt());
                assertEquals(expected, renamed.getReadUpdatedAt());
                assertTrue(renamed.getUpdatedAt() > expected);
                assertEquals(renamed.getName(), name.getText());
                Button delete = (Button) window.getScene().lookup("#deleteBookProperties");
                respondToConfirmation(false);
                delete.fire();
                assertNotNull(getBean(BookRepository.class).getById(original.getId()));
                respondToConfirmation(true);
                delete.fire();
                assertEquals(null, getBean(BookRepository.class).getById(original.getId()));
                assertFalse(books.getItems().stream().anyMatch(book -> book.getId().equals(original.getId())));
                search.setText("несуществующая книга");
                assertTrue(delete.isDisabled());
                assertTrue(books.getItems().isEmpty());
                assertTrue(window.getScene().lookup("#openBookEditor").isDisabled());
            } finally {
                for (Window window : List.copyOf(Window.getWindows())) {
                    window.hide();
                }
                DesignFixture.restore();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(50, TimeUnit.SECONDS);
    }

    @Test
    void dateEditorPreservesMillisecondsAndUsesSelectedZone() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            BookDateEditor editor = new BookDateEditor("testDate", true, java.time.ZoneOffset.ofHours(4), () -> {});
            long original = java.time.Instant.parse("2020-01-02T06:11:12.123Z").toEpochMilli();
            editor.setValue(original);
            assertEquals(original, editor.value());
            DatePicker date = (DatePicker) editor.getChildren().get(0);
            TextField time = (TextField) editor.getChildren().get(1);
            assertEquals(LocalDate.of(2020, 1, 2), date.getValue());
            assertEquals("10:11:12.123", time.getText());
            date.setValue(LocalDate.of(2020, 1, 3));
            assertEquals(original + 86400000L, editor.value());
            time.setText("25:00");
            assertThrows(IllegalArgumentException.class, editor::value);
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
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

    private Stage child(Stage owner) {
        return Window.getWindows().stream().filter(window -> window instanceof Stage stage && stage.getOwner() == owner)
            .map(window -> (Stage) window).findFirst().orElseThrow();
    }

    private void capture(Stage stage) throws Exception {
        WritableImage snapshot = stage.getScene().snapshot(null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        Path destination = Path.of("target", "book-editor.png");
        Files.createDirectories(destination.getParent());
        ImageIO.write(image, "png", destination.toFile());
    }
}
