package com.tsel.home.project.booklibrary.ui;

import com.tsel.home.project.booklibrary.controller.impl.book.BookInfoViewController;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.service.ReferenceService;
import com.tsel.home.project.booklibrary.service.ReferenceService.Entry;
import com.tsel.home.project.booklibrary.service.ReferenceType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReferenceEditorWindow {

    private final Stage stage = new Stage();
    private final ReferenceType type;
    private final ReferenceService service;
    private final Runnable onChange;
    private final TableView<Entry> entries = new TableView<>();
    private final TextField search = new TextField();
    private final ReferencePropertyTable properties;
    private final ListView<Book> books = new ListView<>();
    private final Label count = new Label();
    private final Label status = new Label();
    private final Label deletionHint = new Label();
    private final Button save = new Button("Сохранить");
    private final Button reset = new Button("Отменить изменения");
    private final Button delete = new Button("Удалить");
    private final VBox details = new VBox(10);
    private Entry selected;
    private Map<String, Object> savedProperties = Map.of();
    private boolean updating;

    public ReferenceEditorWindow(Window owner, ReferenceType type, ReferenceService service, Runnable onChange) {
        this.type = type;
        this.service = service;
        this.onChange = onChange;
        properties = new ReferencePropertyTable(type.getEntityClass(), this::updateActions);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        HBox header = new HBox();
        header.setId("windowHeader");
        header.getStyleClass().add("window-header");
        header.setMinHeight(48);
        header.setPrefHeight(48);
        VBox list = createList();
        createDetails();
        HBox content = new HBox(20, list, details);
        content.setPadding(new Insets(16));
        HBox.setHgrow(details, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);
        status.setId("referenceStatus");
        status.getStyleClass().add("reference-status");
        status.setMinHeight(28);
        status.setWrapText(true);
        HBox footer = createFooter();
        VBox root = new VBox(header, content, status, footer);
        root.setPrefSize(880, 600);
        Scene scene = new Scene(root);
        SageTheme.install(stage, scene, type.getTitle(), false);
        scene.getStylesheets().add(getClass().getResource("/com/tsel/home/project/booklibrary/css/library-menu.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(SageTheme.windowIcon());
        stage.setOnCloseRequest(event -> {
            if (!discardChanges()) {
                event.consume();
            }
        });
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                SageTheme.requestClose(stage);
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.S && !save.isDisabled()) {
                save();
                event.consume();
            }
        });
        reload(null);
    }

    public Stage show() {
        WindowPlacement.positionRelativeToMainWindow(stage);
        stage.show();
        search.requestFocus();
        return stage;
    }

    private VBox createList() {
        search.setId("referenceSearch");
        search.setPromptText("Найти запись");
        search.setAccessibleText("Поиск по названию записи");
        search.textProperty().addListener((observable, previous, current) -> {
            if (!updating) {
                if (!discardChanges()) {
                    updating = true;
                    search.setText(previous);
                    updating = false;
                    return;
                }
                reload(selected == null ? null : selected.id());
            }
        });
        entries.setId("referenceEntries");
        entries.setPlaceholder(new Label("Записи не найдены"));
        entries.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Entry, String> title = new TableColumn<>("Запись");
        title.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(value.getValue().name()));
        title.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
            }
        });
        title.setMinWidth(120);
        TableColumn<Entry, Long> total = new TableColumn<>("Книг");
        total.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(value.getValue().bookCount()));
        total.setMinWidth(58);
        total.setMaxWidth(64);
        entries.getColumns().add(title);
        entries.getColumns().add(total);
        entries.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            if (updating || Objects.equals(previous, current)) {
                return;
            }
            if (!discardChanges()) {
                updating = true;
                entries.getSelectionModel().select(previous);
                updating = false;
                return;
            }
            select(current);
        });
        VBox.setVgrow(entries, Priority.ALWAYS);
        VBox list = new VBox(10, search, entries);
        list.setPrefWidth(292);
        list.setMinWidth(292);
        list.setMaxWidth(292);
        return list;
    }

    private void createDetails() {
        count.setId("referenceBookCount");
        count.getStyleClass().add("reference-section-title");
        books.setId("referenceBooks");
        books.getStyleClass().add("reference-books");
        books.setPlaceholder(new Label("Эта запись пока не связана с книгами"));
        books.setCellFactory(list -> {
            ListCell<Book> cell = new ListCell<>() {
                @Override
                protected void updateItem(Book book, boolean empty) {
                    super.updateItem(book, empty);
                    setText(null);
                    setGraphic(null);
                    if (empty || book == null) {
                        return;
                    }
                    Hyperlink link = new Hyperlink(book.getName());
                    link.getStyleClass().add("reference-book-link");
                    link.setAccessibleText("Открыть книгу «" + book.getName() + "»");
                    link.setMinWidth(0);
                    link.setMaxWidth(Double.MAX_VALUE);
                    link.setOnAction(event -> openBook(book.getId()));
                    link.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            openBook(book.getId());
                            event.consume();
                        }
                    });
                    Label identifier = new Label("ID: " + book.getId());
                    identifier.getStyleClass().add("caption");
                    identifier.setMinWidth(Region.USE_PREF_SIZE);
                    HBox row = new HBox(8, link, identifier);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.prefWidthProperty().bind(widthProperty().subtract(12));
                    row.setMaxWidth(Region.USE_PREF_SIZE);
                    HBox.setHgrow(link, Priority.ALWAYS);
                    setGraphic(row);
                }
            };
            cell.prefWidthProperty().bind(list.widthProperty().subtract(18));
            cell.setMaxWidth(Region.USE_PREF_SIZE);
            return cell;
        });
        books.setMinHeight(80);
        books.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && books.getSelectionModel().getSelectedItem() != null) {
                openBook(books.getSelectionModel().getSelectedItem().getId());
                event.consume();
            }
        });
        VBox.setVgrow(books, Priority.ALWAYS);
        deletionHint.setId("referenceDeletionHint");
        deletionHint.setWrapText(true);
        deletionHint.getStyleClass().add("caption");
        deletionHint.setMinHeight(36);
        details.getChildren().addAll(properties, count, books, deletionHint);
    }

    private HBox createFooter() {
        delete.setId("deleteReferenceButton");
        delete.getStyleClass().add("reference-delete");
        delete.setOnAction(event -> delete());
        reset.setId("resetReferenceButton");
        reset.setOnAction(event -> select(selected));
        save.setId("saveReferenceButton");
        save.getStyleClass().add("primary");
        save.setDefaultButton(true);
        save.setOnAction(event -> save());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(10, delete, spacer, reset, save);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");
        return footer;
    }

    private void reload(UUID preferred) {
        String query = normalize(search.getText());
        List<Entry> values = service.entries(type).stream()
            .filter(entry -> normalize(entry.name()).contains(query))
            .toList();
        updating = true;
        entries.getItems().setAll(values);
        Entry next = values.stream().filter(entry -> entry.id().equals(preferred)).findFirst()
            .orElse(values.isEmpty() ? null : values.get(0));
        entries.getSelectionModel().select(next);
        updating = false;
        select(next);
    }

    private void select(Entry value) {
        updating = true;
        selected = value;
        properties.setValue("id", value == null ? null : value.id());
        properties.setValue("name", value == null ? null : value.name());
        properties.setValue("booksInCycle", value == null ? null : value.size());
        properties.setValue("ended", value == null ? null : value.ended());
        savedProperties = properties.values();
        books.getItems().setAll(value == null ? List.of() : service.linkedBooks(type, value.id()));
        count.setText("Связанных книг: " + books.getItems().size());
        details.setDisable(value == null);
        deletionHint.setText(value == null ? "" : books.getItems().isEmpty()
            ? "Связей нет. Запись можно удалить."
            : "Удаление недоступно, пока запись используется книгами.");
        status.setText("");
        status.getStyleClass().remove("error-message");
        updating = false;
        updateActions();
    }

    private boolean dirty() {
        return selected != null && !properties.values().equals(savedProperties);
    }

    private void updateActions() {
        if (updating) {
            return;
        }
        save.setDisable(!dirty());
        reset.setDisable(!dirty());
        delete.setDisable(selected == null || selected.bookCount() > 0);
    }

    private void save() {
        if (selected == null) {
            return;
        }
        try {
            Integer total = type == ReferenceType.CYCLES ? Integer.valueOf(properties.text("booksInCycle")) : null;
            Boolean ended = type == ReferenceType.CYCLES ? properties.flag("ended") : null;
            service.save(type, selected.id(), properties.text("name"), ended, total);
            UUID id = selected.id();
            onChange.run();
            reload(id);
            status.setText("Изменения сохранены");
        } catch (NumberFormatException e) {
            showError("Число книг в цикле должно быть целым и не меньше 0.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Can't save reference entry", e);
            showError("Не удалось сохранить: " + e.getMessage());
        }
    }

    private void delete() {
        if (selected == null || selected.bookCount() > 0) {
            return;
        }
        if (!confirm("Удалить запись?", "«" + selected.name() + "»\nСвязанных книг: 0. Запись будет удалена.", "Удалить")) {
            return;
        }
        try {
            service.delete(type, selected.id());
            onChange.run();
            reload(null);
            status.setText("Запись удалена");
        } catch (RuntimeException e) {
            log.error("Can't delete reference entry", e);
            showError("Не удалось удалить: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (!status.getStyleClass().contains("error-message")) {
            status.getStyleClass().add("error-message");
        }
        status.setText(message);
    }

    private void openBook(UUID id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tsel/home/project/booklibrary/view/info-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage bookWindow = new Stage();
            bookWindow.initOwner(stage);
            bookWindow.initModality(Modality.WINDOW_MODAL);
            bookWindow.setResizable(false);
            SageTheme.install(bookWindow, scene, "Карточка книги", false);
            bookWindow.getIcons().add(SageTheme.windowIcon());
            bookWindow.setScene(scene);
            BookInfoViewController controller = loader.getController();
            controller.initController(loader, null, id);
            if (controller.getBookForView() == null) {
                refreshAfterBookView();
                showError("Книга больше не существует. Список обновлён.");
                return;
            }
            bookWindow.setOnHidden(event -> refreshAfterBookView());
            WindowPlacement.positionRelativeToMainWindow(bookWindow);
            bookWindow.show();
        } catch (Exception e) {
            log.error("Can't open linked book", e);
            showError("Не удалось открыть книгу. Подробности доступны в журнале.");
        }
    }

    private void refreshAfterBookView() {
        try {
            if (selected == null) {
                return;
            }
            UUID id = selected.id();
            Map<String, Object> draft = new LinkedHashMap<>();
            properties.values().forEach((property, value) -> {
                if (!Objects.equals(value, savedProperties.get(property))) {
                    draft.put(property, value);
                }
            });
            onChange.run();
            Entry current = service.entries(type).stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
            if (current != null && !normalize(current.name()).contains(normalize(search.getText()))) {
                updating = true;
                search.clear();
                updating = false;
            }
            reload(id);
            if (selected != null && selected.id().equals(id)) {
                draft.forEach(properties::setValue);
                updateActions();
            }
        } catch (RuntimeException e) {
            log.error("Can't refresh linked books", e);
            showError("Не удалось обновить список книг. Подробности доступны в журнале.");
        }
    }

    private boolean discardChanges() {
        return !dirty() || confirm("Несохранённые изменения", "Изменения записи будут потеряны.", "Отбросить изменения");
    }

    private boolean confirm(String title, String message, String action) {
        return SageDialogs.confirm(stage, title, message, action);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('ё', 'е').trim();
    }
}
