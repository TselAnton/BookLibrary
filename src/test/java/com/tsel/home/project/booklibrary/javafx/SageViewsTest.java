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
                assertCaptionHighlights(main.getScene());
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
                assertTrue(create.getHardCoverCheckBox().getSkin() instanceof com.tsel.home.project.booklibrary.ui.SageCheckBoxSkin);
                assertTrue(create.getAuthorComboBox().getSkin() instanceof com.tsel.home.project.booklibrary.ui.SageComboBoxSkin);
                var tick = (javafx.scene.layout.Region) create.getHardCoverCheckBox().lookup(".mark");
                assertTrue(tick.getShape() instanceof javafx.scene.shape.SVGPath, "Checkbox tick must use the SVG asset");
                create.addBook();
                assertFalse(((Label) formScene.lookup("#validationStatus")).getText().isBlank());
                assertEquals(7, SimpleApplicationContext.getBean(BookRepository.class).getAll().size());
                assertInside(formScene, "#saveBookButton", "#exitButton", "#priceFieldInput", "#coverImagePathFieldInput");
                assertFormControls(formScene, create.getSelectCoverFileButton(), create.getCoverImagePathFieldInput());
                create.getAuthorComboBox().show();
                assertPopupWidth(create.getAuthorComboBox());
                create.stageKeyPressed(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", javafx.scene.input.KeyCode.ESCAPE, false, false, false, false));
                assertFalse(create.getAuthorComboBox().isShowing());
                assertTrue(form.isShowing(), "Escape must close the popup before the form");
                assertSelectionShortcuts(create.getAuthorComboBox());
                create.getPriceFieldInput().setText("1250");
                create.getPriceFieldInput().appendText(".5");
                assertEquals("1250", create.getPriceFieldInput().getText(), "Price entry must use whole rubles");
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
                assertEquals("1251", update.getPriceFieldInput().getText());
                assertInside(editScene, "#saveBookButton", "#priceFieldInput");
                assertFormControls(editScene, update.getSelectCoverFileButton(), update.getCoverImagePathFieldInput());
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
                assertEquals(1250.5, SimpleApplicationContext.getBean(BookRepository.class).getById(id).getPrice(),
                    "Displaying whole rubles must not round a stored fractional value when editing other fields");

                FXMLLoader priceEdit = loader("edit-view.fxml");
                Scene priceScene = new Scene(priceEdit.load());
                EditBookController priceUpdate = priceEdit.getController();
                Stage priceStage = new Stage(); SageTheme.install(priceStage, priceScene, "Изменить книгу", false);
                priceStage.setScene(priceScene); priceUpdate.initController(priceEdit, info, id);
                priceUpdate.configureWindow(priceStage); priceStage.show(); layout(priceStage);
                priceUpdate.getPriceFieldInput().setText("2000"); priceUpdate.editBook();
                assertEquals(2000.0, SimpleApplicationContext.getBean(BookRepository.class).getById(id).getPrice());
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
    private static void assertCaptionHighlights(Scene scene) {
        var hover = javafx.css.PseudoClass.getPseudoClass("hover");
        var focused = javafx.css.PseudoClass.getPseudoClass("focused");
        var keyboardFocus = javafx.css.PseudoClass.getPseudoClass("focus-visible");
        for (Node node : scene.getRoot().lookupAll(".window-header .icon-button")) {
            Button button = (Button) node;
            var previous = java.util.Set.copyOf(button.getPseudoClassStates());
            button.pseudoClassStateChanged(hover, false);
            button.pseudoClassStateChanged(focused, true);
            button.pseudoClassStateChanged(keyboardFocus, false);
            scene.getRoot().applyCss();
            assertEquals(javafx.scene.paint.Color.TRANSPARENT, button.getBackground().getFills().get(0).getFill(),
                "Automatic or mouse focus must not leave caption buttons highlighted");
            button.pseudoClassStateChanged(hover, true);
            scene.getRoot().applyCss();
            assertEquals(javafx.scene.paint.Color.web("#DFE6E0"), button.getBackground().getFills().get(0).getFill(),
                "Close, maximize and minimize must share the same neutral hover color");
            button.pseudoClassStateChanged(hover, false);
            button.pseudoClassStateChanged(keyboardFocus, true);
            scene.getRoot().applyCss();
            assertEquals(javafx.scene.paint.Color.web("#DFE6E0"), button.getBackground().getFills().get(0).getFill());
            for (var state : java.util.List.of(hover, focused, keyboardFocus))
                button.pseudoClassStateChanged(state, previous.contains(state));
        }
        scene.getRoot().applyCss();
    }
    private static void assertFormControls(Scene scene, Button browse, TextField path) {
        assertEquals(path.getHeight(), browse.getHeight(), 0.1, "Browse action must align with the path field");
        assertTrue(((javafx.scene.layout.Region) scene.getRoot()).getBorder().getStrokes().stream()
            .anyMatch(stroke -> stroke.getWidths().getTop() > 0), "Dialog must have a visible boundary");
        assertInside(scene, "#authorComboBox", "#publisherComboBox", "#genreComboBox", "#cycleComboBox");
        assertNotNull(scene.lookup(".currency-suffix LineIcon"));
        for (Node node : scene.getRoot().lookupAll(".window-header .icon-button")) {
            Button caption = (Button) node;
            assertNull(caption.getEffect(), "Caption focus must not draw an extra shadow");
            assertTrue(caption.getBorder() == null || caption.getBorder().getStrokes().stream()
                .allMatch(stroke -> stroke.getWidths().getTop() == 0), "Caption highlight must not add corner borders");
        }
    }
    private static void assertPopupWidth(ComboBox<?> combo) {
        var skin = (javafx.scene.control.skin.ComboBoxListViewSkin<?>) combo.getSkin();
        var popup = (javafx.scene.layout.Region) skin.getPopupContent();
        popup.applyCss(); popup.layout();
        assertEquals(combo.getWidth(), popup.getWidth(), 1, "Long suggestions must fit the combo width");
        for (Node node : popup.lookupAll(".scroll-bar:horizontal")) {
            ScrollBar bar = (ScrollBar) node;
            assertTrue(!bar.isVisible() || bar.getHeight() < 1, "Long suggestions must ellipsize rather than add a horizontal scrollbar");
        }
    }
    private static void assertSelectionShortcuts(ComboBox<?> combo) {
        var editor = combo.getEditor();
        editor.setText("Фрэнк Герберт"); editor.selectAll();
        var originalItems = combo.getItems();
        combo.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_RELEASED,
            "", "", javafx.scene.input.KeyCode.A, false, true, false, false));
        combo.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_RELEASED,
            "", "", javafx.scene.input.KeyCode.CONTROL, false, false, false, false));
        assertEquals("Фрэнк Герберт", editor.getSelectedText(), "Releasing Ctrl after Ctrl+A must preserve selection");
        assertSame(originalItems, combo.getItems(), "Shortcuts must not rebuild suggestions");
        editor.setText("Фр"); editor.positionCaret(1);
        combo.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_RELEASED,
            "", "", javafx.scene.input.KeyCode.R, false, false, false, false));
        assertEquals("Фр", editor.getText()); assertEquals(1, editor.getCaretPosition());
        assertEquals(1, combo.getItems().size());
        editor.clear();
        combo.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_RELEASED,
            "", "", javafx.scene.input.KeyCode.BACK_SPACE, false, false, false, false));
        combo.hide();
    }
    private static void assertInside(Scene scene, String... ids) {
        for (String id : ids) {
            Node node = scene.lookup(id);
            var bounds = node.localToScene(node.getBoundsInLocal());
            assertTrue(bounds.getMaxY() <= scene.getHeight() && bounds.getMinY() >= 0, id + " vertically clipped");
            assertTrue(bounds.getMaxX() <= scene.getWidth() && bounds.getMinX() >= 0, id + " horizontally clipped");
        }
    }
}
