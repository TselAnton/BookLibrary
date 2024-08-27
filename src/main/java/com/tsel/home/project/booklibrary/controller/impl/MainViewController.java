package com.tsel.home.project.booklibrary.controller.impl;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static com.tsel.home.project.booklibrary.utils.table.TableComparators.CHECK_BOX_COMPARATOR;
import static com.tsel.home.project.booklibrary.utils.table.TableComparators.NON_COMPARATOR;
import static com.tsel.home.project.booklibrary.utils.table.TableComparators.STRING_NUMBER_COMPARATOR;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.stage.Modality.NONE;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.SearchServiceV2;
import com.tsel.home.project.booklibrary.utils.elements.ButtonAnswer;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainViewController extends AbstractViewController {

    private static final Logger log = LogManager.getLogger(MainViewController.class);

    private static final Random RANDOM = new Random(1412423095L);
    private static final SearchServiceV2 SEARCH_SERVICE_V2 = SearchServiceV2.INSTANCE;

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
        TABLE_COLUMNS_SETTINGS.put("audioBookSiteGeneratedColumn", "hasAnyAudioBookSite");

        TABLE_COLUMNS_SORTING = new HashMap<>();
        TABLE_COLUMNS_SORTING.put("readColumn", CHECK_BOX_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("cycleEndColumn", CHECK_BOX_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("shelfColumn", STRING_NUMBER_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("nameColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("authorColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("cycleColumn", NON_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("pagesColumn", NON_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("audioBookSiteGeneratedColumn", CHECK_BOX_COMPARATOR);
    }

    private final TableScroll tableScroll = new TableScroll();

    @FXML
    @Getter
    private AnchorPane mainAnchorPane;

    @FXML
    private TextField searchQueryField;

    @FXML
    private TableView<BookDTO> bookTableView;

    @FXML
    private TableColumn<BookDTO, String> cycleColumn;
    private boolean cycleColumnSortedByASC = true;

    @FXML
    private TableColumn<BookDTO, Integer> pagesColumn;
    private boolean pagesColumnSortedByASC = true;

    private Stage lastOpenedBookViewStage;

    /**
     * Запуск окна контроллера
     * @param stage текущий {@link Stage}
     */
    @SuppressWarnings("unchecked")
    public void startScene(Stage stage) {
        try {
            FXMLLoader windowLoader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + "view/main-view.fxml"));
            Scene scene = new Scene(windowLoader.load());

            stage.setTitle("Book library");
            stage.getIcons().add(IMAGE_PROVIDER.getWindowIcon());
            stage.setScene(scene);
            stage.setMinWidth(812);
            stage.setMinHeight(700);

            TableView<BookDTO> initBookTableView = (TableView<BookDTO>) windowLoader.getNamespace().get("bookTableView");
            initTableColumns(windowLoader, initBookTableView);
            updateTableColumns(initBookTableView);
            initBookTableView.getItems().sort(comparing(BookDTO::getName));

            initBookTableView.addEventFilter(ScrollEvent.ANY, scrollEvent -> updateTableScale(scrollEvent, initBookTableView));

            addTooltip("searchHelpButton", "Подсказки для поисковых команд", windowLoader);
            addTooltip("priceButton", "Посчитать стоимость выбранных книг", windowLoader);
            addTooltip("audioBookSiteButton", "Управление сайтами аудиокниг", windowLoader);
            addTooltip("randomBookButton", "Показать случайную книгу из выбранных", windowLoader);

            stage.show();

        } catch (Exception e) {
            log.error("Exception while start scene", e);
        }
    }

    @FXML
    public void addBook() {
        loadModalView("Add new book", "view/add-view.fxml", 400, 15);
        updateTableColumns(bookTableView);
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
                updateTableColumns(bookTableView);
            }
        }
    }

    @FXML
    public void showSearchHelp() {
        riseAlert(
            AlertType.INFORMATION,
            "Подсказки команд для поиска",
            "Подсказки команд для поиска",
            SEARCH_SERVICE_V2.getGeneratedTooltip()
        );
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
        updateTableColumns(bookTableView);
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

        BookDTO randomBook = notReadBooks.get(RANDOM.nextInt(notReadBooks.size()));
        loadBookViewWindow(randomBook.getId());
        updateTableColumns(bookTableView);
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
                    "Книга будет безвозвратно удалена из библиотеки"
                );

                if (answer.isOkAnswer()) {
                    BOOK_REPOSITORY_V2.deleteById(clickedEntity.getId());
                    updateTableColumns(bookTableView);
                }
            }

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            ButtonAnswer answer = riseAlert(CONFIRMATION, "Внимание!", "Закрыть приложение?", "");
            if (answer.isOkAnswer()) {
                System.exit(0);
            }
        }
    }

    //todo: price <= 100 обновляет список после просмотра карточки, нужно починить
    @FXML
    private void search() {
        String searchQuery = searchQueryField.getText();
        bookTableView.setItems(observableArrayList(SEARCH_SERVICE_V2.search(searchQuery, getDtoBooks())));
    }

    @FXML
    private void searchLabelKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            search();
        }
    }

    // TODO: refactoring
    @FXML
    public void onSort() {
        if (bookTableView.getSortOrder().isEmpty()) {
            // Nothing chosen. Sort by book name by default
            bookTableView.getItems().sort(comparing(BookDTO::getName));
        } else {
            // Sort by cycle name + book number in cycle
            if (cycleColumn.equals(bookTableView.getSortOrder().get(0))) {
                if (cycleColumnSortedByASC) {
                    bookTableView.getItems()
                        .sort(comparing(BookDTO::getCycleName, nullsLast(naturalOrder()))
                                .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle)
                        );
                } else {
                    bookTableView.getItems()
                        .sort(comparing(BookDTO::getCycleName, nullsFirst(naturalOrder()))
                            .reversed()
                            .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle)
                        );
                }
                cycleColumnSortedByASC = !cycleColumnSortedByASC;

            } else {
                cycleColumnSortedByASC = true;
            }

            // Sort by pages + !read
            if (pagesColumn.equals(bookTableView.getSortOrder().get(0))) {
                if (pagesColumnSortedByASC) {
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
                pagesColumnSortedByASC = !pagesColumnSortedByASC;

            } else {
                pagesColumnSortedByASC = true;
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
        return BOOK_REPOSITORY_V2.getAll()
            .stream()
            .map(BOOK_CONVERTER::convert)
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

            if (TABLE_COLUMNS_SORTING.containsKey(columnName)) {
                column.setComparator(TABLE_COLUMNS_SORTING.get(columnName));
            }
        });

        // Проставление порядковых номеров для записей
        TableColumn<BookDTO, Number> numberColumn = (TableColumn<BookDTO, Number>) loader.getNamespace().get("numberColumn");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(bookTableView.getItems().indexOf(column.getValue()) + 1));
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
                    updateTableColumns(bookTableView);
                    super.hide();
                }
            };

            stage.setResizable(false);
            stage.setTitle("Book info");
            stage.getIcons().add(IMAGE_PROVIDER.getWindowIcon());
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

    private void updateTableColumns(TableView<BookDTO> bookTableView) {
        bookTableView.setItems(observableArrayList(
            getDtoBooks().stream()
                .sorted(Comparator.comparing(BookDTO::getName))
                .toList()
        ));
    }
}
