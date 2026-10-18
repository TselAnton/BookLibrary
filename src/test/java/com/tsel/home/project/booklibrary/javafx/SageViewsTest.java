package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.controller.impl.book.*;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SageViewsTest extends com.tsel.home.project.booklibrary.UnitJavaFXTest {
    private static final String RESOURCE = "/com/tsel/home/project/booklibrary/view/";

    @Test void realWindowsAndCatalogWorkflow(@TempDir Path directory) throws Exception {
        DesignFixture.initialize(directory);
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                UUID id = DesignFixture.populate();
                Stage main = new Stage();
                new MainViewController().startScene(main);
                layout(main);
                assertEquals(1252, main.getScene().getWidth(), 1);
                TableView<?> table = (TableView<?>) main.getScene().lookup("#bookTableView");
                assertEquals(10, table.getColumns().size());
                assertEquals(7, table.getItems().size());
                assertFalse(main.getScene().lookup("#filterEditor").isManaged());
                ((Button) main.getScene().lookup("#filtersButton")).fire();
                assertTrue(main.getScene().lookup("#filterEditor").isManaged());
                @SuppressWarnings("unchecked") ComboBox<String> filter = (ComboBox<String>) main.getScene().lookup("#readFilter");
                filter.setValue("Прочитано");
                assertEquals(1, table.getItems().size());
                assertTrue(main.getScene().lookup("#activeFilters").isManaged());
                filter.setValue("Все");
                main.setWidth(812); layout(main);
                assertTrue(main.getScene().getRoot().getStyleClass().contains("compact"));
                assertTrue(table.getColumns().stream().allMatch(c -> c.getWidth() >= 20));
                assertTrue(table.getColumns().stream().mapToDouble(TableColumn::getWidth).sum() <= 780,
                    "All columns must fit the compact viewport");

                FXMLLoader add = loader("add-view.fxml");
                Scene formScene = new Scene(add.load());
                NewBookController create = add.getController();
                Stage form = new Stage();
                SageTheme.install(form, formScene, "Добавить книгу", false);
                form.setScene(formScene); create.configureWindow(form); form.show(); layout(form);
                assertEquals(484, formScene.getWidth(), 1); assertEquals(675, formScene.getHeight(), 1);
                assertTrue(create.getCycleEndedCheckBox().isDisabled());
                create.addBook();
                assertFalse(((Label) formScene.lookup("#validationStatus")).getText().isBlank());
                assertEquals(7, SimpleApplicationContext.getBean(BookRepository.class).getAll().size());
                assertInside(formScene, "#saveBookButton", "#exitButton", "#priceFieldInput", "#coverImagePathFieldInput");
                create.getAuthorComboBox().show();
                create.stageKeyPressed(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", javafx.scene.input.KeyCode.ESCAPE, false, false, false, false));
                assertFalse(create.getAuthorComboBox().isShowing());
                assertTrue(form.isShowing(), "Escape must close the popup before the form");
                create.getNameTextFieldInput().setText("Черновик");
                Platform.runLater(() -> {
                    Stage alert = (Stage) Window.getWindows().stream().filter(w -> w instanceof Stage s
                        && "Несохранённые изменения".equals(s.getTitle())).findFirst().orElseThrow();
                    DialogPane pane = (DialogPane) alert.getScene().lookup(".dialog-pane");
                    ButtonType cancel = pane.getButtonTypes().stream().filter(b -> b.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                        .findFirst().orElseThrow();
                    ((Button) pane.lookupButton(cancel)).fire();
                });
                SageTheme.requestClose(form);
                assertTrue(form.isShowing(), "Canceling discard must preserve the form");
                assertEquals("Черновик", create.getNameTextFieldInput().getText());
                create.getNameTextFieldInput().setText("Новая книга");
                create.getAuthorComboBox().getEditor().setText("Новый автор");
                create.getPublisherComboBox().getEditor().setText("Новый издатель");
                create.getPagesCountFieldInput().setText("0");
                create.getPublicationYearFieldInput().setText("0");
                create.getPriceFieldInput().setText("0");
                create.addBook();
                assertEquals(8, SimpleApplicationContext.getBean(BookRepository.class).getAll().size());

                FXMLLoader details = loader("info-view.fxml");
                Scene detailsScene = new Scene(details.load());
                BookInfoViewController info = details.getController();
                Stage detailStage = new Stage();
                SageTheme.install(detailStage, detailsScene, "Карточка книги", false);
                detailStage.setScene(detailsScene); info.initController(details, null, id); detailStage.show(); layout(detailStage);
                assertEquals(812, detailsScene.getWidth(), 1); assertEquals(600, detailsScene.getHeight(), 1);
                assertEquals("Нет", ((Label) detailsScene.lookup("#readLabel")).getText());
                assertTrue(detailsScene.lookup("#missingCover").isVisible());
                assertInside(detailsScene, "#deleteBookButton", "#editBookButton", "#cycleEndedLabel");

                FXMLLoader edit = loader("edit-view.fxml");
                Scene editScene = new Scene(edit.load());
                EditBookController update = edit.getController();
                Stage editStage = new Stage(); SageTheme.install(editStage, editScene, "Изменить книгу", false);
                editStage.setScene(editScene); update.initController(edit, info, id); update.configureWindow(editStage); editStage.show(); layout(editStage);
                assertEquals("Дюна", update.getNameTextFieldInput().getText());
                assertFalse(update.getCycleEndedCheckBox().isDisabled());
                assertEquals("Z:/missing-cover.png", update.getCoverImagePathFieldInput().getText());
                assertInside(editScene, "#saveBookButton", "#priceFieldInput");
                Platform.runLater(() -> {
                    Window.getWindows().stream().filter(w -> w instanceof Stage s && "Несохранённые изменения".equals(s.getTitle()))
                        .findFirst().ifPresent(w -> {
                            DialogPane pane = (DialogPane) w.getScene().lookup(".dialog-pane");
                            pane.getButtonTypes().stream().filter(b -> b.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                                .findFirst().ifPresent(b -> ((Button) pane.lookupButton(b)).fire());
                        });
                });
                SageTheme.requestClose(editStage);
                assertFalse(editStage.isShowing(), "A populated but untouched edit form must close without a discard prompt");
                editStage.show();
                update.getReadCheckBox().setSelected(true);
                update.getNumberInCycleFieldInput().setText("20");
                update.editBook();
                assertTrue(SimpleApplicationContext.getBean(BookRepository.class).getById(id).getRead());
                assertEquals("Да", ((Label) detailsScene.lookup("#readLabel")).getText());
                assertTrue(((Label) detailsScene.lookup("#cycleLabel")).getText().contains("20 из 6"));
            } finally {
                for (Window w : java.util.List.copyOf(Window.getWindows())) w.hide();
                DesignFixture.restore();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(40, TimeUnit.SECONDS);
    }

    private static FXMLLoader loader(String file) { return new FXMLLoader(SageViewsTest.class.getResource(RESOURCE + file)); }
    private static void layout(Stage stage) { stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); }
    private static void assertInside(Scene scene, String... ids) {
        for (String id : ids) {
            Node node = scene.lookup(id);
            var bounds = node.localToScene(node.getBoundsInLocal());
            assertTrue(bounds.getMaxY() <= scene.getHeight() && bounds.getMinY() >= 0, id + " vertically clipped");
            assertTrue(bounds.getMaxX() <= scene.getWidth() && bounds.getMinX() >= 0, id + " horizontally clipped");
        }
    }
}
