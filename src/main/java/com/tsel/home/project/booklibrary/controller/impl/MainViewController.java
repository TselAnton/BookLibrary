package com.tsel.home.project.booklibrary.controller.impl;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static com.tsel.home.project.booklibrary.utils.table.TableComparators.CHECK_BOX_COMPARATOR;
import static com.tsel.home.project.booklibrary.utils.table.TableComparators.NON_COMPARATOR;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.stage.Modality.NONE;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.model.FilterState;
import com.tsel.home.project.booklibrary.ui.LineIcon;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import com.tsel.home.project.booklibrary.ui.filter.FilterChips;
import com.tsel.home.project.booklibrary.ui.filter.FilterDrawer;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditorRegistry;
import com.tsel.home.project.booklibrary.utils.table.ButtonAnswer;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainViewController extends AbstractViewController {

    private static final Random RANDOM = new Random(1412423095L);

    @SuppressWarnings("rawtypes")
    private static final Map<String, Comparator> TABLE_COLUMNS_SORTING;
    private static final Map<String, String> TABLE_COLUMNS_SETTINGS;

    static {
        TABLE_COLUMNS_SETTINGS = new HashMap<>();
        TABLE_COLUMNS_SETTINGS.put("nameColumn", "name");
        TABLE_COLUMNS_SETTINGS.put("authorColumn", "author");
        TABLE_COLUMNS_SETTINGS.put("publisherColumn", "publisher");
        TABLE_COLUMNS_SETTINGS.put("cycleColumn", "cycleName");
        TABLE_COLUMNS_SETTINGS.put("cycleNumberColumn", "cycleNumber");
        TABLE_COLUMNS_SETTINGS.put("cycleEndColumn", "cycleEnded");
        TABLE_COLUMNS_SETTINGS.put("readColumn", "read");
        TABLE_COLUMNS_SETTINGS.put("pagesColumn", "pages");
        TABLE_COLUMNS_SETTINGS.put("imageColumn", "cover");


        TABLE_COLUMNS_SORTING = new HashMap<>();
        TABLE_COLUMNS_SORTING.put("readColumn", CHECK_BOX_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("cycleEndColumn", CHECK_BOX_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("nameColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("authorColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("cycleColumn", NON_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("pagesColumn", NON_COMPARATOR);
    }

    private final TableScroll tableScroll = getBean(TableScroll.class);
    private final FilterCatalog filterCatalog = new FilterCatalog(BookDTO.class);
    private final FilterState filterState = new FilterState();

    @FXML
    @Getter
    private AnchorPane mainAnchorPane;

    @FXML
    private TableView<BookDTO> bookTableView;

    @FXML
    private TableColumn<BookDTO, String> cycleColumn;

    @FXML
    private TableColumn<BookDTO, Integer> pagesColumn;

    private Stage lastOpenedBookViewStage;
    @FXML
    private Button addBookButton;

    @FXML
    private Button filtersButton;

    @FXML
    private VBox activeFilterContainer;

    @FXML
    private Label resultStatus;

    private FilterChips activeFilters;
    private FilterDrawer filterDrawer;
    private List<BookDTO> catalogBooks = List.of();
    private boolean compact;


    /**
     * Запуск окна контроллера
     * @param stage текущий {@link Stage}
     */
    public void startScene(Stage stage) {
        try {
            cleanUpRepositoryData();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(RESOURCE_PATH + "view/main-view.fxml"));
            Scene scene = new Scene(loader.load());
            MainViewController controller = loader.getController();
            controller.configure(stage, scene, loader);
            stage.show();
            scene.getRoot().applyCss();
            for (javafx.scene.Node node : scene.getRoot().lookupAll(".column-header .label")) {
                if (node instanceof Label label && !label.getText().isBlank())
                    Tooltip.install(label, new Tooltip(label.getText()));
            }
        } catch (Exception e) {
            log.error("Exception while starting main window", e);
            throw new IllegalStateException("Не удалось открыть библиотеку", e);
        }
    }

    private void configure(Stage stage, Scene scene, FXMLLoader loader) {
        SageTheme.install(stage, scene, "Моя библиотека", true);
        stage.getIcons().add(imageProvider.getWindowIcon());
        stage.setScene(scene);
        stage.setMinWidth(812);
        stage.setMinHeight(700);
        initTableColumns(loader, bookTableView);
        bookTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        bookTableView.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) { updateTableScale(event, bookTableView); event.consume(); }
        });
        bookTableView.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> updateStatus());
        activeFilters = new FilterChips(this::resetFilters);
        activeFilterContainer.getChildren().add(activeFilters);
        filterDrawer = new FilterDrawer(
            filterCatalog, filterState, FilterEditorRegistry.defaults(),
            () -> catalogBooks,
            this::applyFilters, () -> {
                updateFilterButton();
                filtersButton.requestFocus();
            }
        );
        AnchorPane.setTopAnchor(filterDrawer, 84.0);
        AnchorPane.setBottomAnchor(filterDrawer, 28.0);
        AnchorPane.setLeftAnchor(filterDrawer, 0.0);
        AnchorPane.setRightAnchor(filterDrawer, 0.0);
        mainAnchorPane.getChildren().add(filterDrawer);
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> filterDrawer.dispose());
        stage.widthProperty().addListener((o, a, b) -> updateCompact(b.doubleValue()));
        updateCompact(1252);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (filterDrawer.isOpen()) {
                return;
            }
            if (e.isControlDown() && e.getCode() == KeyCode.F) { toggleFilters(); e.consume(); }
            if (e.isControlDown() && e.getCode() == KeyCode.N) { addBook(); e.consume(); }
        });
        addTooltip("priceButton", "Посчитать стоимость найденных книг", loader);
        addTooltip("randomBookButton", "Случайная непрочитанная книга из найденных", loader);
        addBookButton.setTooltip(new Tooltip("Добавить книгу (Ctrl+N)"));
        refreshCatalog();
    }

    private void updateCompact(double width) {
        compact = width < 1050;
        if (compact) { if (!mainAnchorPane.getStyleClass().contains("compact")) mainAnchorPane.getStyleClass().add("compact"); }
        else mainAnchorPane.getStyleClass().remove("compact");
        addBookButton.setText(compact ? "" : "Добавить книгу");
        updateFilterButton();
        double[] widths = compact ? new double[]{24,28,154,112,58,112,64,76,80,72}
            : new double[]{36,40,268,190,90,180,80,88,88,80};
        double available = Math.max(740, width - 48);
        double total = java.util.Arrays.stream(widths).sum();
        for (int i = 0; i < widths.length; i++) {
            TableColumn<BookDTO, ?> column = bookTableView.getColumns().get(i);
            column.setReorderable(false);
            double size = Math.floor(widths[i] * available / total);
            column.setMinWidth(size); column.setMaxWidth(size); column.setPrefWidth(size);
        }
        bookTableView.refresh();
    }

    /**
     * Переключает панель; закрытие отбрасывает только черновик, а не применённые условия.
     */
    @FXML
    public void toggleFilters() {
        if (filterDrawer.isOpen()) {
            filterDrawer.cancel();
        } else {
            filterDrawer.open();
        }
        updateFilterButton();
    }

    /**
     * Снимает все условия, обновляет таблицу и прогревает варианты для полного каталога.
     */
    @FXML
    public void resetFilters() {
        filterState.clear();
        applyFilters();
    }

    private void updateFilterButton() {
        int count = filterState.applied().size();
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);
        content.getChildren().add(new LineIcon("filter"));
        if (!compact) {
            content.getChildren().add(new Label("Фильтры"));
        }
        if (count > 0) {
            Label badge = new Label(Integer.toString(count));
            badge.getStyleClass().add("filter-count");
            content.getChildren().add(badge);
        }
        if (!compact) {
            LineIcon chevron = new LineIcon("down");
            chevron.setRotate(filterDrawer.isOpen() ? 180 : 0);
            content.getChildren().add(chevron);
        }
        filtersButton.setText(null);
        filtersButton.setGraphic(content);
        filtersButton.setAccessibleText("Фильтры, активно: " + count + (filterDrawer.isOpen() ? ", закрыть" : ", открыть"));
        filtersButton.setTooltip(new Tooltip(filtersButton.getAccessibleText()));
    }

    private void applyFilters() {
        UUID selected = bookTableView.getSelectionModel().getSelectedItem() == null ? null
            : bookTableView.getSelectionModel().getSelectedItem().getId();
        List<BookDTO> all = catalogBooks;
        List<BookDTO> results = filterCatalog.filter(all, filterState.applied());
        results = results.stream().sorted(comparing(BookDTO::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        bookTableView.setItems(observableArrayList(results));
        bookTableView.getItems().stream()
            .filter(book -> book.getId().equals(selected))
            .findFirst()
            .ifPresent(book -> bookTableView.getSelectionModel().select(book));
        if (!bookTableView.getSortOrder().isEmpty()) {
            bookTableView.sort();
        }
        activeFilters.update(filterCatalog, filterState.applied(), field -> {
            filterState.remove(field);
            applyFilters();
        });
        filterDrawer.refreshOptions();
        updateFilterButton();
        updateStatus();
        VBox empty = new VBox(8);
        empty.setAlignment(Pos.CENTER);
        empty.getChildren().add(new Label(all.isEmpty() ? "Нет книг" : "Ничего не найдено"));
        Button action = new Button(all.isEmpty() ? "Добавить книгу" : "Сбросить");
        action.setOnAction(event -> {
            if (all.isEmpty()) {
                addBook();
            } else {
                resetFilters();
            }
        });
        empty.getChildren().add(action);
        bookTableView.setPlaceholder(empty);
    }

    private void updateStatus() {
        resultStatus.setText("Найдено: " + bookTableView.getItems().size() + " из " + bookRepository.getAll().size()
            + "   |   Выбрано: " + (bookTableView.getSelectionModel().getSelectedItem() == null ? 0 : 1));
    }

    @FXML public void exitApplication() { SageTheme.requestClose((Stage) mainAnchorPane.getScene().getWindow()); }
    @FXML public void openSelectedBook() {
        BookDTO selected = bookTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (lastOpenedBookViewStage != null) lastOpenedBookViewStage.close();
            loadBookViewWindow(selected.getId());
        }
    }

    @Override public void updateControllerState(UUID entityId) { refreshCatalog(); }

    @FXML
    public void addBook() {
        Stage mainStage = (Stage) mainAnchorPane.getScene().getWindow();
        int offsetY = (int) Math.round(resolveCoordinate(Stage::getY, 150) - mainStage.getY()) - 32;
        loadModalView("Добавить книгу", "view/add-view.fxml", 240, offsetY);
        refreshCatalog();
    }

    @FXML
    public void clickOnTable(MouseEvent mouseEvent) {
        if (isDoubleClick(mouseEvent)) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();

            if (clickedEntity != null) {
                if (lastOpenedBookViewStage != null) {
                    lastOpenedBookViewStage.close();
                }

                loadBookViewWindow(clickedEntity.getId());
                refreshCatalog();
            }
        }
    }

    @FXML
    public void showBooksPrice() {
        double fullPrice = bookTableView.getItems()
            .stream()
            .map(BookDTO::getPrice)
            .filter(Objects::nonNull)
            .reduce(Double::sum)
            .orElse(0.0);

        int itemsSize = bookTableView.getItems().size();

        riseAlert(AlertType.INFORMATION, "Стоимость книг", "Стоимость книг",
            format("Стоимость %s %s: %s руб",
                itemsSize,
                itemsSize == 1 ? "книги" : "книг",
                new DecimalFormat("###,###").format(fullPrice)
            )
        );
    }

    @FXML
    private void manageAudioBookSites() {
        loadModalView(
            "Audiobook sites",
            "view/audio-book-sites-view.fxml",
            500,
            50
        );
        refreshCatalog();
    }

    @FXML
    public void chooseRandomBook() {
        List<BookDTO> notReadBooks = bookTableView.getItems()
            .stream()
            .filter(bookDTO -> !bookDTO.getRead().isSelected())
            .toList();

        if (lastOpenedBookViewStage != null) {
            lastOpenedBookViewStage.close();
        }

        if (notReadBooks.isEmpty()) {
            riseAlert(AlertType.INFORMATION, "Случайная книга", "Нет непрочитанных книг", "Измените фильтры.");
            return;
        }
        BookDTO randomBook = notReadBooks.get(RANDOM.nextInt(notReadBooks.size()));
        loadBookViewWindow(randomBook.getId());
        refreshCatalog();
    }

    @FXML
    public void tableViewKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();

            if (clickedEntity != null) {
                ButtonAnswer answer = riseAlert(
                    CONFIRMATION,
                    "Внимание!",
                    "Вы уверены?",
                    "Удалить книгу «" + clickedEntity.getName() + "»?"
                );

                if (answer.isOkAnswer()) {
                    bookRepository.delete(bookRepository.getById(clickedEntity.getId()));
                    refreshCatalog();
                }
            }

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            openSelectedBook(); keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            ButtonAnswer answer = riseAlert(CONFIRMATION, "Внимание!", "Закрыть приложение?", "");
            if (answer.isOkAnswer()) {
                exitApplication();
            }
        }
    }

    /**
     * Перечитывает каталог без сброса применённых условий и раскрытия тегов.
     */
    @FXML
    public void refreshBooks() {
        refreshCatalog();
    }

    // TODO: refactoring
    @FXML
    public void onSort() {
        if (bookTableView.getSortOrder().isEmpty()) {
            // Nothing chosen. Sort by book name by default
            bookTableView.getItems().sort(comparing(BookDTO::getName));
            return;
        }

        // Sort by cycle name + book number in cycle
        if (cycleColumn.equals(bookTableView.getSortOrder().get(0))) {
            if (cycleColumn.getSortType() == TableColumn.SortType.ASCENDING) {
                bookTableView.getItems()
                  .sort(comparing(
                      BookDTO::getCycleName,
                      nullsLast(naturalOrder())
                  ).thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle));
            } else {
                bookTableView.getItems()
                  .sort(comparing(BookDTO::getCycleName, nullsFirst(naturalOrder()))
                    .reversed()
                    .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle)
                  );
            }
        }

        // Sort by pages + !read
        if (pagesColumn.equals(bookTableView.getSortOrder().get(0))) {
            if (pagesColumn.getSortType() == TableColumn.SortType.ASCENDING) {
                bookTableView.getItems()
                  .sort(comparing(BookDTO::getRead, (c1, c2) -> Boolean.compare(c1.isSelected(), c2.isSelected()))
                    .thenComparing(BookDTO::getPages)
                  );

            } else {
                bookTableView.getItems()
                  .sort(comparing(BookDTO::getRead, (c1, c2) -> Boolean.compare(c2.isSelected(), c1.isSelected()))
                    .thenComparing(BookDTO::getPages)
                    .reversed()
                  );
            }
        }
    }

    private int comparatorByBookNumberInCycle(String bookNumber1, String bookNumber2) {
        Integer number1 = getBookNumberInCycle(bookNumber1);
        Integer number2 = getBookNumberInCycle(bookNumber2);

        if (number1 == null) {
            return number2 == null ? 0 : -1;
        }

        return number2 == null ? 1 : Integer.compare(number1, number2);
    }

    private Integer getBookNumberInCycle(String bookNumber) {
        return isNotBlank(bookNumber)
            ? Integer.parseInt(bookNumber.split("/")[0])
            : null;
    }

    private List<BookDTO> getDtoBooks() {
        return bookRepository.getAll()
            .stream()
            .map(bookConverter::convert)
            .toList();
    }

    private double resolveCoordinate(ToDoubleFunction<Stage> getStageCoordinate, double defaultCoordinate) {
        return lastOpenedBookViewStage != null
            ? getStageCoordinate.applyAsDouble(lastOpenedBookViewStage)
            : defaultCoordinate;
    }

    @SuppressWarnings("unchecked")
    private void initTableColumns(FXMLLoader loader, TableView<BookDTO> bookTableView) {
        TABLE_COLUMNS_SETTINGS.forEach((columnName, fieldName) -> {
            TableColumn<BookDTO, ?> column = (TableColumn<BookDTO, ?>) loader.getNamespace().get(columnName);
            column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
            installCellFactory(column);

            if (TABLE_COLUMNS_SORTING.containsKey(columnName)) {
                column.setComparator(TABLE_COLUMNS_SORTING.get(columnName));
            }
        });

        // Проставление порядковых номеров для записей
        TableColumn<BookDTO, Number> numberColumn = (TableColumn<BookDTO, Number>) loader.getNamespace().get("numberColumn");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(bookTableView.getItems().indexOf(column.getValue()) + 1));
        numberColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void updateTableScale(ScrollEvent scrollEvent, TableView<BookDTO> bookTableView) {
        if (scrollEvent.isControlDown()) {
            bookTableView.setStyle(tableScroll.scroll(scrollEvent));
        }
    }

    private void addTooltip(String buttonName, String tooltipText, FXMLLoader loader) {
        Button button = (Button) loader.getNamespace().get(buttonName);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setAutoHide(false);
        tooltip.setFont(new Font(13f));
        tooltip.setShowDelay(new Duration(200f));

        Tooltip.install(button, tooltip);
    }

    private boolean isDoubleClick(MouseEvent mouseEvent) {
        return MouseButton.PRIMARY.equals(mouseEvent.getButton()) && mouseEvent.getClickCount() >= 2;
    }

    private void loadBookViewWindow(UUID bookId) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + "view/info-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage() {
                @Override
                public void hide() {
                    refreshCatalog();
                    super.hide();
                }
            };

            stage.setResizable(false);
            SageTheme.install(stage, scene, "Карточка книги", false);
            stage.getIcons().add(imageProvider.getWindowIcon());
            stage.setScene(scene);
            stage.initModality(NONE);

            Stage primaryStage = (Stage) mainAnchorPane.getScene().getWindow();
            stage.initOwner(primaryStage);

            stage.setX(resolveCoordinate(Stage::getX, 550));
            stage.setY(resolveCoordinate(Stage::getY, 150));

            if (bookId != null) {
                AbstractViewController controller = loader.getController();
                controller.initController(loader, this, bookId);
            }

            stage.show();
            lastOpenedBookViewStage = stage;

        } catch (Exception e) {
            log.error("Exception while load model view", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void installCellFactory(TableColumn<BookDTO, ?> column) {
        ((TableColumn) column).setCellFactory(c -> new TableCell<BookDTO, Object>() {
            {
                if (java.util.Set.of("imageColumn", "cycleNumberColumn", "cycleEndColumn", "readColumn", "pagesColumn").contains(column.getId()))
                    getStyleClass().add("numeric-cell");
            }
            @Override protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                setText(null); setGraphic(null); setTooltip(null);
                if (empty) return;
                setAlignment(Pos.CENTER_LEFT);
                if (java.util.Set.of("cycleNumberColumn", "cycleEndColumn", "readColumn", "pagesColumn").contains(column.getId()))
                    setAlignment(Pos.CENTER);
                if (value instanceof javafx.scene.image.ImageView image) {
                    if (!compact) {
                        BookDTO book = getTableRow().getItem();
                        if (book != null && Boolean.TRUE.equals(book.getHasCoverImage())) {
                            image.setFitWidth(24); image.setFitHeight(32); image.setPreserveRatio(true); setGraphic(image);
                        } else setGraphic(new LineIcon("book"));
                    }
                } else if (value instanceof CheckBox check) {
                    setAlignment(Pos.CENTER); setGraphic(check); check.setFocusTraversable(false); check.setMouseTransparent(true);
                    setAccessibleText(check.isSelected() ? "Да" : "Нет");
                } else {
                    String text = value == null ? "—" : value.toString();
                    if ("cycleNumberColumn".equals(column.getId())) text = text.replace("/", " из ");
                    setText(text); setTooltip(new Tooltip(text));
                }
            }
        });
    }

    /**
     * Обновляет DTO, повторно применяет фильтры и запускает фоновый прогрев их вариантов.
     * Один путь используется после добавления, изменения, удаления и ручного обновления.
     */
    private void refreshCatalog() {
        if (filterDrawer != null) {
            catalogBooks = getDtoBooks();
            applyFilters();
        }
    }

    /**
     * Чистка из репозиториев всех записей, не связанных с book
     */
    private void cleanUpRepositoryData() {
        try {
            Set<UUID> usedCycleIds = new HashSet<>();
            Set<UUID> usedGenreIds = new HashSet<>();
            Set<UUID> usedAuthorIds = new HashSet<>();
            Set<UUID> usedPublisherIds = new HashSet<>();

            for (Book book : bookRepository.getAll()) {
                if (book.getCycleId() != null) usedCycleIds.add(book.getCycleId());
                if (book.getGenreId() != null) usedGenreIds.add(book.getGenreId());
                if (book.getAuthorId() != null) usedAuthorIds.add(book.getAuthorId());
                if (book.getPublisherId() != null) usedPublisherIds.add(book.getPublisherId());
            }

            cleanUpRepository(cycleRepository, usedCycleIds);
            cleanUpRepository(genreRepository, usedGenreIds);
            cleanUpRepository(authorRepository, usedAuthorIds);
            cleanUpRepository(publisherRepository, usedPublisherIds);

        } catch (Exception e) {
            log.error("Exception while cleaning repository data", e);
        }
    }

    private <T extends BaseEntity<UUID>, R extends FileRepository<UUID, T>> void cleanUpRepository(R repository, Set<UUID> userIdSet) {
        repository.getAll()
            .stream()
            .map(BaseEntity::getId)
            .filter(id -> !userIdSet.contains(id))
            .forEach(repository::deleteById);
    }
}
