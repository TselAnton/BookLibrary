package com.tsel.home.project.booklibrary.ui;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.controller.impl.book.EditBookController;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.service.BookPropertiesService;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BookEditorWindow {

    private final Stage stage = new Stage();
    private final BookRepository repository = getBean(BookRepository.class);
    private final BookPropertiesService service = new BookPropertiesService(repository);
    private final AbstractViewController parent;
    private final Runnable onChange;
    private final TableView<Book> books = new TableView<>();
    private final TextField search = new TextField();
    private final GridPane form = new GridPane();
    private final Map<Field, Node> editors = new LinkedHashMap<>();
    private final Label status = new Label();
    private final Button save = new Button("Сохранить");
    private final Button reset = new Button("Отменить изменения");
    private final Button delete = new Button("Удалить");
    private final Hyperlink open = new Hyperlink("Открыть на редактирование");
    private Map<String, Object> initial = Map.of();
    private Book selected;
    private boolean updating;

    public BookEditorWindow(Window owner, AbstractViewController parent, Runnable onChange) {
        this.parent = parent;
        this.onChange = onChange;
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        HBox header = new HBox();
        header.setId("windowHeader");
        header.getStyleClass().add("window-header");
        header.setMinHeight(48);
        setupList();
        setupForm();
        VBox list = new VBox(10, search, books);
        list.setPrefWidth(300);
        list.setMinWidth(240);
        VBox.setVgrow(books, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(610);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox details = new VBox(8, scroll);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox content = new HBox(16, list, details);
        content.setPadding(new Insets(16));
        VBox.setVgrow(content, Priority.ALWAYS);
        open.setId("openBookEditor");
        open.setOnAction(event -> openEditor());
        save.setId("saveBookProperties");
        save.getStyleClass().add("primary");
        save.setOnAction(event -> save());
        reset.setId("resetBookProperties");
        reset.setOnAction(event -> select(selected));
        Button close = new Button("Закрыть");
        close.setOnAction(event -> SageTheme.requestClose(stage));
        list.getChildren().add(close);
        delete.setId("deleteBookProperties");
        delete.getStyleClass().add("reference-delete");
        delete.setOnAction(event -> delete());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, delete, spacer, reset, save);
        VBox footer = new VBox(8, open, status, actions);
        details.getChildren().add(footer);
        status.setId("bookPropertiesStatus");
        status.setWrapText(true);
        VBox root = new VBox(header, content);
        root.setPrefSize(980, 720);
        Scene scene = new Scene(root, 980, 720);
        SageTheme.install(stage, scene, "Правка книг", false);
        scene.getStylesheets().add(getClass().getResource("/com/tsel/home/project/booklibrary/css/library-menu.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(SageTheme.windowIcon());
        stage.setResizable(false);
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
        return stage;
    }

    private void setupList() {
        search.setId("bookPropertiesSearch");
        search.setPromptText("Найти книгу или автора");
        search.textProperty().addListener((observable, previous, current) -> {
            if (updating) {
                return;
            }
            if (!discardChanges()) {
                updating = true;
                search.setText(previous);
                updating = false;
                return;
            }
            reload(selected == null ? null : selected.getId());
        });
        books.setId("bookPropertiesList");
        books.setPlaceholder(new Label("Книги не найдены"));
        books.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Book, String> title = new TableColumn<>("Название");
        title.setMinWidth(150);
        title.setPrefWidth(180);
        title.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(value.getValue().getName()));
        TableColumn<Book, String> author = new TableColumn<>("Автор");
        author.setMinWidth(110);
        author.setPrefWidth(120);
        author.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(authorName(value.getValue())));
        books.getColumns().add(title);
        books.getColumns().add(author);
        books.getSelectionModel().selectedItemProperty().addListener((observable, previous, current) -> {
            if (updating || current == selected) {
                return;
            }
            if (!discardChanges()) {
                updating = true;
                // Restore after the selection model finishes its current change.
                Platform.runLater(() -> {
                    books.getSelectionModel().select(selected);
                    updating = false;
                });
                return;
            }
            select(current);
        });
    }

    private void setupForm() {
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(8));
        int row = 0;
        List<String> order = List.of("id", "name", "genreId", "authorId", "publisherId", "pages", "read", "readUpdatedAt",
            "autograph", "cycleId", "numberInSeries", "coverImgAbsolutePath", "hardCover", "price", "publicationYear",
            "audioBookSiteIds", "createdAt", "updatedAt");
        List<Field> fields = Arrays.stream(Book.class.getDeclaredFields())
            .sorted(Comparator.comparingInt(field -> order.contains(field.getName()) ? order.indexOf(field.getName()) : order.size()))
            .toList();
        for (Field field : fields) {
            Property property = field.getAnnotation(Property.class);
            if (property == null) {
                continue;
            }
            field.setAccessible(true);
            Node editor;
            if (field.getType() == Long.class) {
                editor = new BookDateEditor("bookProperty" + field.getName(), property.nullable(), ZoneId.systemDefault(), this::updateActions);
            } else if (field.getType() == Boolean.class) {
                CheckBox flag = new CheckBox();
                flag.selectedProperty().addListener((observable, previous, current) -> updateActions());
                editor = flag;
            } else {
                TextField text = new TextField();
                text.setEditable(field.getType() != UUID.class && field.getType() != List.class);
                if (!text.isEditable()) {
                    text.getStyleClass().add("reference-read-only");
                }
                text.textProperty().addListener((observable, previous, current) -> updateActions());
                editor = text;
            }
            editor.setId("bookProperty" + field.getName());
            editor.setAccessibleText(property.value());
            editors.put(field, editor);
            Label label = new Label(Character.toUpperCase(property.value().charAt(0)) + property.value().substring(1));
            label.setWrapText(true);
            label.setPrefWidth(155);
            label.setLabelFor(editor);
            form.addRow(row++, label, editor);
            GridPane.setHgrow(editor, Priority.ALWAYS);
        }
        CheckBox read = (CheckBox) editors.entrySet().stream().filter(entry -> entry.getKey().getName().equals("read"))
            .findFirst().orElseThrow().getValue();
        editors.entrySet().stream().filter(entry -> entry.getKey().getName().equals("readUpdatedAt"))
            .findFirst().orElseThrow().getValue().disableProperty().bind(read.selectedProperty().not());
    }

    private void delete() {
        if (selected == null || !SageDialogs.confirm(stage, "Удаление книги",
            "Удалить «" + selected.getName() + "»? Книга будет безвозвратно удалена из библиотеки.", "Удалить")) {
            return;
        }
        try {
            repository.delete(repository.getById(selected.getId()));
            getBean(ImageProvider.class).deleteImagesFromCache(selected);
            onChange.run();
            reload(null);
        } catch (RuntimeException e) {
            log.error("Can't delete book", e);
            status.setText("Не удалось удалить книгу: " + e.getMessage());
        }
    }

    private void reload(UUID preferred) {
        String query = normalize(search.getText());
        List<Book> values = repository.getAll().stream()
            .filter(book -> normalize(book.getName() + " " + authorName(book)).contains(query))
            .sorted(Comparator.comparing(Book::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
        updating = true;
        books.getItems().setAll(values);
        Book next = values.stream().filter(book -> book.getId().equals(preferred)).findFirst()
            .orElse(values.isEmpty() ? null : values.get(0));
        books.getSelectionModel().select(next);
        updating = false;
        select(next);
    }

    private void select(Book book) {
        updating = true;
        selected = book;
        for (Map.Entry<Field, Node> entry : editors.entrySet()) {
            try {
                Object value = book == null ? null : entry.getKey().get(book);
                Node editor = entry.getValue();
                if (editor instanceof BookDateEditor date) {
                    date.setValue((Long) value);
                } else if (editor instanceof CheckBox flag) {
                    flag.setSelected(Boolean.TRUE.equals(value));
                } else if (editor instanceof TextField text) {
                    text.setText(book == null ? "" : text.isEditable() ? Objects.toString(value, "") : linkName(book, entry.getKey().getName()));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        initial = values();
        form.setDisable(book == null);
        delete.setDisable(book == null);
        open.setDisable(book == null);
        status.setText("");
        updating = false;
        updateActions();
    }

    private Map<String, Object> values() {
        Map<String, Object> result = new LinkedHashMap<>();
        editors.forEach((field, editor) -> {
            if (editor instanceof BookDateEditor date) {
                result.put(field.getName(), date.state());
            } else if (editor instanceof CheckBox flag) {
                result.put(field.getName(), Boolean.toString(flag.isSelected()));
            } else if (editor instanceof TextField text) {
                result.put(field.getName(), text.getText());
            }
        });
        return result;
    }

    private boolean dirty() {
        return selected != null && !values().equals(initial);
    }

    private void updateActions() {
        if (!updating) {
            save.setDisable(!dirty());
            reset.setDisable(!dirty());
        }
    }

    private void save() {
        if (selected == null) {
            return;
        }
        try {
            Book edited = repository.getById(selected.getId());
            if (edited == null) {
                throw new IllegalStateException("Книга больше не существует");
            }
            Set<String> dates = new HashSet<>();
            Map<String, Object> current = values();
            for (Map.Entry<Field, Node> entry : editors.entrySet()) {
                Field field = entry.getKey();
                if (Objects.equals(initial.get(field.getName()), current.get(field.getName()))) {
                    continue;
                }
                Node editor = entry.getValue();
                if (editor instanceof BookDateEditor date) {
                    if (date.isDisabled()) {
                        continue;
                    }
                    field.set(edited, date.value());
                    dates.add(field.getName());
                } else if (editor instanceof CheckBox flag) {
                    field.set(edited, flag.isSelected());
                } else if (editor instanceof TextField text && text.isEditable()) {
                    String value = text.getText().trim();
                    try {
                        Object parsed = value.isEmpty() ? null : value;
                        if (!value.isEmpty() && field.getType() == Integer.class) {
                            parsed = Integer.valueOf(value);
                        } else if (!value.isEmpty() && field.getType() == Double.class) {
                            parsed = Double.valueOf(value.replace(',', '.'));
                        }
                        field.set(edited, parsed);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Неверное число: " + field.getAnnotation(Property.class).value(), e);
                    }
                }
            }
            Book saved = service.save(selected, edited, dates);
            getBean(ImageProvider.class).deleteImagesFromCache(saved);
            onChange.run();
            reload(saved.getId());
            status.setText("Изменения сохранены");
        } catch (Exception e) {
            log.error("Can't save book properties", e);
            status.setText("Не удалось сохранить: " + e.getMessage());
        }
    }

    private void openEditor() {
        if (selected == null || !discardChanges()) {
            return;
        }
        UUID id = selected.getId();
        select(selected);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tsel/home/project/booklibrary/view/edit-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage editor = new Stage();
            editor.initOwner(stage);
            editor.initModality(Modality.WINDOW_MODAL);
            SageTheme.install(editor, scene, "Изменить книгу", false);
            editor.setScene(scene);
            editor.getIcons().add(SageTheme.windowIcon());
            EditBookController controller = loader.getController();
            controller.initController(loader, parent, id);
            controller.configureWindow(editor);
            editor.setOnHidden(event -> {
                onChange.run();
                reload(id);
            });
            WindowPlacement.positionRelativeToMainWindow(editor);
            editor.show();
        } catch (Exception e) {
            log.error("Can't open book editor", e);
            status.setText("Не удалось открыть редактор: " + e.getMessage());
        }
    }

    private boolean discardChanges() {
        return !dirty() || SageDialogs.confirm(stage, "Несохранённые изменения", "Изменения книги будут потеряны.", "Отбросить изменения");
    }

    private String authorName(Book book) {
        return Optional.ofNullable(getBean(AuthorRepository.class).getById(book.getAuthorId()))
            .map(value -> value.getName()).orElse("");
    }

    private String linkName(Book book, String field) {
        return switch (field) {
            case "id" -> book.getId().toString();
            case "authorId" -> authorName(book);
            case "publisherId" -> Optional.ofNullable(getBean(PublisherRepository.class).getById(book.getPublisherId())).map(value -> value.getName()).orElse("");
            case "genreId" -> Optional.ofNullable(getBean(GenreRepository.class).getById(book.getGenreId())).map(value -> value.getName()).orElse("");
            case "cycleId" -> Optional.ofNullable(getBean(CycleRepository.class).getById(book.getCycleId())).map(value -> value.getName()).orElse("");
            case "audioBookSiteIds" -> book.getAudioBookSiteIds() == null ? "" : book.getAudioBookSiteIds().stream()
                .map(id -> getBean(AudioBookSiteRepository.class).getById(id)).filter(Objects::nonNull)
                .map(value -> value.getName()).collect(java.util.stream.Collectors.joining(", "));
            default -> "";
        };
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('ё', 'е').trim();
    }
}
