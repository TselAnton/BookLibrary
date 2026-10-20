package com.tsel.home.project.booklibrary.javafx;

import static org.junit.jupiter.api.Assertions.*;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.controller.impl.book.BookInfoViewController;
import com.tsel.home.project.booklibrary.controller.impl.book.EditBookController;
import com.tsel.home.project.booklibrary.controller.impl.book.NewBookController;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import com.tsel.home.project.booklibrary.ui.LineIcon;
import com.tsel.home.project.booklibrary.ui.SageDialogs;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import com.tsel.home.project.booklibrary.ui.filter.FilterChips;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SageViewsTest extends com.tsel.home.project.booklibrary.UnitJavaFXTest {
    private static final String RESOURCE = "/com/tsel/home/project/booklibrary/view/";

    @Test void realWindowsAndCatalogWorkflow(@TempDir Path directory) throws Exception {
        DesignFixture.initialize(directory);
        String reducedMotion = System.getProperty("booklibrary.reducedMotion");
        System.setProperty("booklibrary.reducedMotion", "true");
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                UUID id = DesignFixture.populate();
                Stage main = new Stage();
                new MainViewController().startScene(main);
                awaitFilterOptions(main.getScene());
                layout(main);
                assertCaptionHighlights(main.getScene());
                assertEquals(1252, main.getScene().getWidth(), 1);
                TableView<?> table = (TableView<?>) main.getScene().lookup("#bookTableView");
                assertEquals(10, table.getColumns().size());
                assertEquals(7, table.getItems().size());
                assertSearchWorkflow(main, table);
                assertDialogs(main);
                assertAudioDialogs(main);
                assertFalse(main.getScene().lookup("#filterEditor").isManaged());
                ((Button) main.getScene().lookup("#filtersButton")).fire();
                assertTrue(main.getScene().lookup("#filterEditor").isManaged());
                CheckBox filter = (CheckBox) main.getScene().lookup("#filter-read-option-0");
                filter.fire();
                assertEquals(7, table.getItems().size(), "Draft filters must not change the table");
                ((Button) main.getScene().lookup("#applyFiltersButton")).fire();
                assertEquals(1, table.getItems().size());
                assertTrue(main.getScene().lookup("#activeFilters").isManaged());
                ((Button) main.getScene().lookup("#resetFiltersButton")).fire();
                assertFiltersWorkflow(main, table);
                assertCatalogCacheLifecycle(main, table);
                assertAsyncCoverFilters(main, table);
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
                assertNull(create.getCoverImagePathFieldInput().getTooltip());
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
                assertTrue(detailsScene.getRoot().lookupAll(".label").stream()
                    .map(Label.class::cast).allMatch(label -> label.getTooltip() == null));
                ImageView cover = (ImageView) detailsScene.lookup("#coverImage");
                assertNotNull(cover.getImage());
                assertTrue(SimpleApplicationContext.getBean(ImageProvider.class).isPlaceholder(cover.getImage()));
                assertInside(detailsScene, "#deleteBookButton", "#editBookButton", "#cycleEndedLabel");
                Button delete = button(detailsScene, "#deleteBookButton");
                Button close = button(detailsScene, "#closeBookButton");
                assertEquals(close.getTextFill(), delete.getTextFill());
                assertEquals(close.getBackground().getFills(), delete.getBackground().getFills());
                assertEquals(close.getBorder().getStrokes(), delete.getBorder().getStrokes());
                capture(detailsScene, "filters-book-details");

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
                if (reducedMotion == null) {
                    System.clearProperty("booklibrary.reducedMotion");
                } else {
                    System.setProperty("booklibrary.reducedMotion", reducedMotion);
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(40, TimeUnit.SECONDS);
    }

    private static void assertAsyncCoverFilters(Stage main, TableView<?> table) {
        Scene scene = main.getScene();
        BookDTO book = (BookDTO) table.getItems().get(0);
        ImageProvider provider = SimpleApplicationContext.getBean(ImageProvider.class);
        ImageProvider.ImageHolder original = provider.resolveSmallCover(
            SimpleApplicationContext.getBean(BookRepository.class).getById(book.getId())
        );
        button(scene, "#filtersButton").fire();
        selectChoice(scene, "hasCoverImage", "Есть");
        button(scene, "#applyFiltersButton").fire();
        assertTrue(table.getItems().isEmpty());

        // Publish a completed image through the same property used by the background loader.
        book.getCover().imageProperty().unbind();
        book.getCover().setImage(new WritableImage(2, 2));
        settle(main, 180);
        assertEquals(1, table.getItems().size(), "Loaded covers must reapply the active image filter");
        assertSame(book, table.getItems().get(0));
        button(scene, "#filtersButton").fire();
        awaitFilterOptions(scene);
        CheckBox present = scene.lookup("#filter-hasCoverImage").lookupAll(".filter-option").stream()
            .map(CheckBox.class::cast).filter(choice -> "Есть".equals(choice.getText())).findFirst().orElseThrow();
        assertEquals("(1)", ((Label) present.getGraphic()).getText(), "Cover counts must invalidate cached snapshots");
        button(scene, "#cancelFiltersButton").fire();

        book.getCover().imageProperty().bind(original.imageProperty());
        settle(main, 180);
        assertTrue(table.getItems().isEmpty());
        button(scene, "#resetFiltersButton").fire();
        assertEquals(7, table.getItems().size());
    }

    private static void assertCatalogCacheLifecycle(Stage main, TableView<?> table) throws Exception {
        Scene scene = main.getScene();
        button(scene, "#filtersButton").fire();
        text(scene, "#filter-name-value").setText("Кеш:");
        button(scene, "#applyFiltersButton").fire();
        assertTrue(table.getItems().isEmpty());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Stage form = shownStage("Добавить книгу");
                Scene add = form.getScene();
                text(add, "#nameTextFieldInput").setText("Кеш: новая книга");
                ((ComboBox<?>) add.lookup("#authorComboBox")).getEditor().setText("Кеш-автор");
                ((ComboBox<?>) add.lookup("#publisherComboBox")).getEditor().setText("Кеш-издатель");
                text(add, "#pagesCountFieldInput").setText("10");
                text(add, "#publicationYearFieldInput").setText("2024");
                text(add, "#priceFieldInput").setText("0");
                button(add, "#saveBookButton").fire();
            } catch (Throwable error) {
                failure.set(error);
                List.copyOf(Window.getWindows()).stream().filter(window -> window != main).forEach(Window::hide);
            }
        });
        button(scene, "#addBookButton").fire();
        assertNull(failure.get());
        assertEquals(1, table.getItems().size(), "Creating a matching book must reapply the active filters");
        assertCachedAuthor(scene, "Кеш-автор", 1);
        table.getSelectionModel().selectFirst();
        table.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        Stage details = shownStage("Карточка книги");
        assertEquals("Подарок", ((Label) details.getScene().lookup("#priceLabel")).getText());
        Platform.runLater(() -> {
            try {
                Scene edit = shownStage("Изменить книгу").getScene();
                text(edit, "#nameTextFieldInput").setText("Кеш: изменённая книга");
                ((ComboBox<?>) edit.lookup("#authorComboBox")).getEditor().setText("Кеш-автор изменённый");
                text(edit, "#priceFieldInput").setText("250");
                button(edit, "#saveBookButton").fire();
            } catch (Throwable error) {
                failure.set(error);
                List.copyOf(Window.getWindows()).stream()
                    .filter(window -> window != main && window != details).forEach(Window::hide);
            }
        });
        button(details.getScene(), "#editBookButton").fire();
        assertNull(failure.get());
        assertEquals("Кеш: изменённая книга", ((BookDTO) table.getItems().get(0)).getName());
        assertEquals("250 ₽", ((Label) details.getScene().lookup("#priceLabel")).getText());
        assertCachedAuthor(scene, "Кеш-автор изменённый", 1);
        Platform.runLater(() -> {
            DialogPane pane = (DialogPane) shownStage("Удаление книги").getScene().lookup(".dialog-pane");
            ButtonType accept = pane.getButtonTypes().stream()
                .filter(type -> type.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
            ((Button) pane.lookupButton(accept)).fire();
        });
        button(details.getScene(), "#deleteBookButton").fire();
        assertFalse(details.isShowing());
        assertTrue(table.getItems().isEmpty(), "Deleting a matching book must reapply the active filters");
        button(scene, "#filtersButton").fire();
        awaitFilterOptions(scene);
        assertFalse(scene.lookup("#filter-author").lookupAll(".filter-option").stream()
            .map(CheckBox.class::cast).anyMatch(choice -> choice.getText().startsWith("Кеш-автор")),
            "Deleted and replaced authors must disappear from the warmed option domain");
        button(scene, "#cancelFiltersButton").fire();
        button(scene, "#resetFiltersButton").fire();
        assertEquals(7, table.getItems().size());
    }

    private static Stage shownStage(String title) {
        return Window.getWindows().stream().filter(window -> window instanceof Stage stage
            && stage.isShowing() && title.equals(stage.getTitle())).map(Stage.class::cast).findFirst().orElseThrow();
    }

    private static void assertCachedAuthor(Scene scene, String author, long count) {
        button(scene, "#filtersButton").fire();
        awaitFilterOptions(scene);
        CheckBox choice = scene.lookup("#filter-author").lookupAll(".filter-option").stream()
            .map(CheckBox.class::cast).filter(value -> author.equals(value.getText())).findFirst().orElseThrow();
        assertEquals("(" + count + ")", ((Label) choice.getGraphic()).getText());
        button(scene, "#cancelFiltersButton").fire();
    }

    private static void assertFiltersWorkflow(Stage main, TableView<?> table) throws Exception {
        Scene scene = main.getScene();
        button(scene, "#filtersButton").fire();
        layout(main);
        assertNull(scene.lookup("#filter-hasAnyAudioBookSite"));
        assertEquals(13, scene.getRoot().lookupAll(".filter-section").size());
        for (String field : List.of("pages", "publicationYear")) {
            ComboBox<?> operations = (ComboBox<?>) scene.lookup("#filter-" + field + "-operation");
            assertFalse(operations.getItems().contains(NumericOperation.EMPTY));
            assertFalse(operations.getItems().contains(NumericOperation.NOT_EMPTY));
            assertEquals(7, operations.getItems().size());
        }
        for (String field : List.of("hardCover", "read", "autograph")) {
            assertEquals(2, scene.lookup("#filter-" + field).lookupAll(".filter-option").size());
        }
        ScrollPane scroll = (ScrollPane) scene.lookup("#filterScroll");
        assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
        double footerY = scene.lookup("#filterFooter").localToScene(0, 0).getY();
        scroll.setVvalue(1);
        layout(main);
        assertEquals(footerY, scene.lookup("#filterFooter").localToScene(0, 0).getY(), 0.1);
        assertInside(scene, "#applyFiltersButton", "#cancelFiltersButton");
        Button applyButton = button(scene, "#applyFiltersButton");
        applyButton.requestFocus();
        applyButton.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
        assertSame(scene.lookup("#closeFiltersButton"), scene.getFocusOwner(), "Tab stays inside the drawer");
        scene.getFocusOwner().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, true, false, false, false));
        assertSame(applyButton, scene.getFocusOwner(), "Shift+Tab stays inside the drawer");
        CheckBox unread = (CheckBox) scene.lookup("#filter-read-option-1");
        unread.fire();
        awaitFilterOptions(scene);
        assertEquals("(1)", ((Label) ((CheckBox) scene.lookup("#filter-read-option-0")).getGraphic()).getText());
        assertEquals("(6)", ((Label) unread.getGraphic()).getText());
        assertEquals(7, table.getItems().size());
        button(scene, "#cancelFiltersButton").fire();
        button(scene, "#filtersButton").fire();
        assertFalse(scene.lookup("#filterLoading").isVisible(), "Reopening a warmed filter panel must reuse cached options");
        assertFalse(((CheckBox) scene.lookup("#filter-read-option-1")).isSelected());
        text(scene, "#filter-publicationYear-first").setText("2020");
        text(scene, "#filter-publicationYear-second").setText("2000");
        button(scene, "#applyFiltersButton").fire();
        assertTrue(scene.lookup("#filterEditor").isVisible());
        assertFalse(((Label) scene.lookup("#filterError")).getText().isBlank());
        assertEquals(7, table.getItems().size());
        text(scene, "#filter-publicationYear-first").setText("1965");
        text(scene, "#filter-publicationYear-second").setText("1966");
        button(scene, "#applyFiltersButton").fire();
        assertEquals(2, table.getItems().size());
        ((Button) scene.lookup("#chip-publicationYear").lookup(".chip-close")).fire();
        assertEquals(7, table.getItems().size());

        button(scene, "#filtersButton").fire();
        @SuppressWarnings("unchecked")
        ComboBox<NumericOperation> priceOperation = (ComboBox<NumericOperation>) scene.lookup("#filter-price-operation");
        priceOperation.setValue(NumericOperation.EQUALS);
        text(scene, "#filter-price-first").setText("1250,5");
        button(scene, "#applyFiltersButton").fire();
        assertEquals(1, table.getItems().size());
        button(scene, "#resetFiltersButton").fire();

        button(scene, "#filtersButton").fire();
        assertEquals("Бесценно", priceOperation.getConverter().toString(NumericOperation.EMPTY));
        assertEquals("Стоимость указана", priceOperation.getConverter().toString(NumericOperation.NOT_EMPTY));
        priceOperation.setValue(NumericOperation.EMPTY);
        button(scene, "#applyFiltersButton").fire();
        assertEquals(1, table.getItems().size());
        assertEquals("1984", ((BookDTO) table.getItems().get(0)).getName());
        assertTrue(((Label) scene.lookup("#chip-price").lookup(".label")).getText().contains("Бесценно"));
        button(scene, "#filtersButton").fire();
        priceOperation.setValue(NumericOperation.NOT_EMPTY);
        button(scene, "#applyFiltersButton").fire();
        assertEquals(6, table.getItems().size());
        button(scene, "#resetFiltersButton").fire();

        button(scene, "#filtersButton").fire();
        for (Node heading : scene.getRoot().lookupAll(".filter-heading")) {
            if (!List.of("filter-read-heading", "filter-autograph-heading", "filter-hardCover-heading").contains(heading.getId())) {
                ((Button) heading).fire();
            }
        }
        scroll.setVvalue(0.55);
        layout(main);
        capture(scene, "filters-boolean");
        button(scene, "#cancelFiltersButton").fire();
        button(scene, "#filtersButton").fire();
        for (String field : List.of("name", "author", "publisher", "pages", "publicationYear", "genre", "cycleName", "cycleEnded", "hasCoverImage", "price")) {
            button(scene, "#filter-" + field + "-heading").fire();
        }
        text(scene, "#filter-name-value").setText("Дюн");
        selectChoice(scene, "author", "Фрэнк Герберт");
        selectChoice(scene, "publisher", "АСТ");
        text(scene, "#filter-pages-first").setText("0");
        text(scene, "#filter-pages-second").setText("1000");
        text(scene, "#filter-publicationYear-first").setText("1900");
        text(scene, "#filter-publicationYear-second").setText("2100");
        selectChoice(scene, "read", "Не прочитано");
        selectChoice(scene, "autograph", "Нет");
        selectChoice(scene, "hardCover", "Есть");
        selectChoice(scene, "genre", "Фантастика");
        selectChoice(scene, "cycleName", "Хроники Дюны");
        selectChoice(scene, "cycleEnded", "Завершён");
        selectChoice(scene, "hasCoverImage", "Нет");
        text(scene, "#filter-price-first").setText("0");
        text(scene, "#filter-price-second").setText("9999");
        button(scene, "#applyFiltersButton").fire();
        assertEquals(2, table.getItems().size());
        main.setWidth(812);
        settle(main);
        assertEquals(812, scene.getWidth(), 1);
        FilterChips chips = (FilterChips) scene.lookup("#activeFilters");
        assertTrue(button(scene, "#expandFiltersButton").isVisible());
        assertFalse(chips.isExpanded());
        Node cachedNameEditor = scene.lookup("#filter-name");
        Button expandTags = button(scene, "#expandFiltersButton");
        assertTrue(expandTags.getText() == null || expandTags.getText().isEmpty());
        assertEquals(0, expandTags.getGraphic().getRotate(), 0.1);
        assertTrue(expandTags.getGraphic() instanceof LineIcon);
        Label hiddenCount = (Label) scene.lookup("#hiddenFiltersCount");
        assertTrue(hiddenCount.isVisible());
        long hiddenTags = scene.getRoot().lookupAll(".filter-chip").stream().filter(node -> !node.isVisible()).count();
        assertEquals(Long.toString(hiddenTags), hiddenCount.getText());
        assertTagActionsDoNotOverlap(main);
        double collapsedHeight = chips.getHeight();
        capture(scene, "filters-tags-collapsed");
        button(scene, "#expandFiltersButton").fire();
        layout(main);
        assertTrue(chips.isExpanded());
        assertEquals(180, expandTags.getGraphic().getRotate(), 0.1);
        assertFalse(hiddenCount.isVisible());
        assertTagActionsDoNotOverlap(main);
        assertEquals(button(scene, "#resetFiltersButton").localToScene(0, 0).getY(), expandTags.localToScene(0, 0).getY(), 1);
        assertTrue(chips.getHeight() > collapsedHeight);
        capture(scene, "filters-tags-expanded");
        table.getSelectionModel().select(0);
        table.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        Stage details = (Stage) Window.getWindows().stream()
            .filter(window -> window instanceof Stage stage && "Карточка книги".equals(stage.getTitle()))
            .findFirst().orElseThrow();
        assertTrue(chips.isExpanded());
        AtomicReference<Throwable> editFailure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Stage edit = (Stage) Window.getWindows().stream()
                    .filter(window -> window instanceof Stage stage && "Изменить книгу".equals(stage.getTitle()))
                    .findFirst().orElseThrow();
                text(edit.getScene(), "#pagesCountFieldInput").setText("2000");
                button(edit.getScene(), "#saveBookButton").fire();
            } catch (Throwable e) {
                editFailure.set(e);
                List.copyOf(Window.getWindows()).stream().filter(window -> window != main && window != details).forEach(Window::hide);
            }
        });
        button(details.getScene(), "#editBookButton").fire();
        assertNull(editFailure.get(), "Editing the book through its window must succeed");
        assertEquals(1, table.getItems().size(), "Existing filters must be reapplied after editing");
        details.hide();
        layout(main);
        assertTrue(chips.isExpanded(), "Closing details/edit must preserve expanded tags");
        button(scene, "#filtersButton").fire();
        assertSame(cachedNameEditor, scene.lookup("#filter-name"), "Opening filters must reuse existing editors");
        ((ComboBox<?>) scene.lookup("#filter-pages-operation")).show();
        scene.lookup("#filter-pages").fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
        assertTrue(scene.lookup("#filterEditor").isVisible(), "First Escape closes the combo popup");
        scene.lookup("#filter-pages").fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
        assertFalse(scene.lookup("#filterEditor").isVisible());
        assertTrue(chips.isExpanded());
        button(scene, "#resetFiltersButton").fire();
        assertNotNull(scene.lookup("#searchQueryField"));
        assertEquals(7, table.getItems().size());
        scene.getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F, false, true, false, false));
        assertSame(scene.lookup("#searchQueryField"), scene.getFocusOwner(), "Ctrl+F must focus search");
        button(scene, "#filtersButton").fire();
        text(scene, "#filter-name-value").setText("Дюна");
        selectChoice(scene, "read", "Не прочитано");
        button(scene, "#applyFiltersButton").fire();
        assertEquals(1, table.getItems().size());
        assertEquals("Дюна", ((BookDTO) table.getItems().get(0)).getName());
        ((MenuBar) scene.lookup(".menu-bar")).getMenus().get(1).getItems().get(2).fire();
        Stage genreEditor = shownStage("Редактировать жанры");
        text(genreEditor.getScene(), "#referenceName").appendText(" (правка)");
        button(genreEditor.getScene(), "#saveReferenceButton").fire();
        genreEditor.close();
        assertEquals(1, table.getItems().size(), "Editing references must preserve the applied filters");
        button(scene, "#resetFiltersButton").fire();
        assertEquals(7, table.getItems().size());
        button(scene, "#filtersButton").fire();
        CheckBox longAuthor = scene.lookup("#filter-author").lookupAll(".filter-option").stream()
            .map(CheckBox.class::cast)
            .filter(choice -> choice.getText().startsWith("Очень длинное"))
            .findFirst().orElseThrow();
        longAuthor.fire();
        button(scene, "#applyFiltersButton").fire();
        layout(main);
        Node chip = scene.lookup("#chip-author");
        Label label = (Label) chip.lookup(".label");
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1, "A long tag must show its complete wrapped text");
        assertInside(scene, "#chip-author");
        assertTrue(chip.lookup(".chip-close").localToScene(chip.lookup(".chip-close").getBoundsInLocal()).getMaxX() <= scene.getWidth());
        capture(scene, "filters-long-tag");
        button(scene, "#resetFiltersButton").fire();
        System.setProperty("booklibrary.reducedMotion", "false");
        button(scene, "#filtersButton").fire();
        assertTrue(scene.lookup("#filterPanel").getTranslateX() > 0);
        settle(main, 180);
        assertEquals(0, scene.lookup("#filterPanel").getTranslateX(), 0.1);
        button(scene, "#cancelFiltersButton").fire();
        settle(main, 180);
        assertFalse(scene.lookup("#filterEditor").isVisible());
        System.setProperty("booklibrary.reducedMotion", "true");
    }

    private static void settle(Stage stage) {
        settle(stage, 80);
    }

    private static void assertSearchWorkflow(Stage main, TableView<?> table) throws Exception {
        Scene scene = main.getScene();
        TextField search = text(scene, "#searchQueryField");
        Button clear = button(scene, "#clearSearchButton");
        assertFalse(clear.isVisible());
        search.setText("  ДЮН  ");
        assertEquals(2, table.getItems().size());
        assertTrue(clear.isVisible());
        button(scene, "#filtersButton").fire();
        selectChoice(scene, "read", "Не прочитано");
        button(scene, "#applyFiltersButton").fire();
        assertEquals(2, table.getItems().size());
        search.setText("несуществующая книга");
        assertEquals(0, table.getItems().size());
        clear.fire();
        assertEquals(6, table.getItems().size(), "Clearing search must preserve applied filters");
        assertFalse(clear.isVisible());
        assertSame(search, scene.getFocusOwner());
        button(scene, "#resetFiltersButton").fire();
        assertEquals(7, table.getItems().size());
        main.setWidth(812);
        settle(main);
        search.setText("Герберт");
        layout(main);
        assertInside(scene, "#searchQueryField", "#clearSearchButton", "#filtersButton", "#randomBookButton");
        assertTrue(search.getWidth() >= 160);
        capture(scene, "search-compact");
        clear.fire();
        main.setWidth(1252);
        settle(main);
        layout(main);
        assertTrue(scene.getRoot().lookupAll(".table-cell").stream()
            .map(javafx.scene.control.TableCell.class::cast).allMatch(cell -> cell.getTooltip() == null));
        capture(scene, "search-main");
    }

    private static void assertDialogs(Stage owner) throws Exception {
        ButtonType accept = new ButtonType("Продолжить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        for (Alert.AlertType type : List.of(Alert.AlertType.CONFIRMATION, Alert.AlertType.ERROR,
            Alert.AlertType.WARNING, Alert.AlertType.INFORMATION)) {
            String details = type == Alert.AlertType.ERROR ? "Очень длинное сообщение об ошибке. ".repeat(100) : "Дополнительные сведения.";
            Alert dialog = SageDialogs.create(owner, type, "Проверка сообщения", "Удалить «Зелёная миля»?", details,
                type == Alert.AlertType.CONFIRMATION ? new ButtonType[] {accept, cancel} : new ButtonType[] {accept});
            try {
                dialog.show();
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                layout(stage);
                assertSame(owner, stage.getOwner());
                assertEquals(StageStyle.UNDECORATED, stage.getStyle());
                assertEquals(1, stage.getScene().getRoot().lookupAll("#windowHeader").size());
                assertTrue(stage.getHeight() < 520, "Long messages must scroll within a compact dialog");
                for (ButtonType buttonType : dialog.getButtonTypes()) {
                    Node button = dialog.getDialogPane().lookupButton(buttonType);
                    assertTrue(button.localToScene(button.getBoundsInLocal()).getMaxY() <= stage.getScene().getHeight());
                }
                capture(stage.getScene(), "dialog-" + type.name().toLowerCase(java.util.Locale.ROOT));
                if (type == Alert.AlertType.CONFIRMATION) {
                    assertTrue(((Button) dialog.getDialogPane().lookupButton(cancel)).isDefaultButton());
                    assertFalse(((Button) dialog.getDialogPane().lookupButton(accept)).isDefaultButton());
                    Button close = (Button) stage.getScene().lookup("#windowHeader").lookupAll(".icon-button").iterator().next();
                    close.fire();
                    assertEquals(cancel, dialog.getResult(), "The caption close action must cancel");
                } else {
                    ((Button) dialog.getDialogPane().lookupButton(accept)).fire();
                    assertEquals(accept, dialog.getResult());
                }
            } finally {
                dialog.close();
            }
        }
    }

    private static void assertAudioDialogs(Stage owner) throws Exception {
        for (String file : List.of("audio-book-sites-view.fxml", "audio-book-sites-connections-view.fxml",
            "audio-book-sites-connections-edit-view.fxml")) {
            FXMLLoader loader = loader(file);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.initOwner(owner);
            SageTheme.install(stage, scene, "Сайты аудиокниг", false);
            stage.setScene(scene);
            try {
                AbstractViewController controller = loader.getController();
                controller.initController(loader, null, new java.util.ArrayList<UUID>());
                stage.show();
                layout(stage);
                assertEquals(StageStyle.UNDECORATED, stage.getStyle());
                assertNotNull(scene.lookup("#windowHeader .icon-button"));
                assertInside(scene, "#audioBookSiteTable");
                for (Node node : scene.getRoot().lookupAll(".button")) {
                    assertTrue(node.localToScene(node.getBoundsInLocal()).getMaxY() <= scene.getHeight());
                }
                capture(scene, file.replace(".fxml", ""));
            } finally {
                stage.hide();
            }
        }
    }

    private static void settle(Stage stage, double milliseconds) {
        Object key = new Object();
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.exitNestedEventLoop(key, null));
        pause.play();
        Platform.enterNestedEventLoop(key);
        layout(stage);
    }

    private static Button button(Scene scene, String selector) {
        if (selector.startsWith("#filter-")) {
            awaitFilterOptions(scene);
        }
        Node node = scene.lookup(selector);
        assertNotNull(node, selector);
        return (Button) node;
    }

    private static TextField text(Scene scene, String selector) {
        awaitFilterOptions(scene);
        Node node = scene.lookup(selector);
        assertNotNull(node, selector);
        return (TextField) node;
    }

    private static void selectChoice(Scene scene, String field, String label) {
        awaitFilterOptions(scene);
        CheckBox choice = scene.lookup("#filter-" + field).lookupAll(".filter-option").stream()
            .map(CheckBox.class::cast)
            .filter(checkBox -> label.equals(checkBox.getText()))
            .findFirst().orElseThrow();
        choice.fire();
    }

    private static void awaitFilterOptions(Scene scene) {
        Node loading = scene.lookup("#filterLoading");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (loading != null && loading.isVisible()) {
            assertTrue(System.nanoTime() < deadline, "Filter values must finish preparing");
            settle((Stage) scene.getWindow(), 10);
        }
    }

    private static void assertTagActionsDoNotOverlap(Stage main) {
        Scene scene = main.getScene();
        for (int width : new int[]{900, 1050, 1252, 1000, 840, 812}) {
            main.setWidth(width);
            settle(main, 40);
            Node reset = scene.lookup("#resetFiltersButton");
            Node expand = scene.lookup("#expandFiltersButton");
            Node count = scene.lookup("#hiddenFiltersCount");
            double resetLeft = reset.localToScene(reset.getBoundsInLocal()).getMinX();
            assertTrue(expand.localToScene(expand.getBoundsInLocal()).getMaxX() + 4 <= resetLeft,
                "Expand action must not overlap reset at width " + width);
            if (count.isVisible()) {
                assertTrue(count.localToScene(count.getBoundsInLocal()).getMaxX() + 4 <= resetLeft,
                    "Hidden tag count must not overlap reset at width " + width);
            }
            assertTrue(reset.localToScene(reset.getBoundsInLocal()).getMaxX() <= scene.getWidth());
        }
    }

    private static void capture(Scene scene, String name) throws Exception {
        if (!Boolean.getBoolean("booklibrary.captureFilters")) {
            return;
        }
        WritableImage image = scene.getRoot().snapshot(null, null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        Path output = Path.of("target", name + ".ppm");
        try (OutputStream stream = Files.newOutputStream(output)) {
            stream.write(("P6\n" + width + " " + height + "\n255\n").getBytes(StandardCharsets.US_ASCII));
            byte[] row = new byte[width * 3];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getPixelReader().getArgb(x, y);
                    row[x * 3] = (byte) (pixel >> 16);
                    row[x * 3 + 1] = (byte) (pixel >> 8);
                    row[x * 3 + 2] = (byte) pixel;
                }
                stream.write(row);
            }
        }
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
