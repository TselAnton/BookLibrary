package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.stage.Modality.NONE;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.SearchServiceV2;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainViewController extends AbstractViewController {

    private static final String TABLE_SCALE_PATTERN = "-fx-font-size: %spt";
    private static final int MIN_FONT = 9;
    private static final int MAX_FONT = 16;

    private static final SearchServiceV2 SEARCH_SERVICE_V2 = SearchServiceV2.INSTANCE;

    private static final Comparator<String> STRING_NUMBER_COMPARATOR = comparingInt(Integer::parseInt);
    private static final Comparator<Object> NON_COMPARATOR = (x1, x2) -> 0;
    private static final Comparator<CheckBox> CHECK_BOX_COMPARATOR = (c1, c2) -> {
        if (c1 == null) {
            return c2 == null ? 0 : 1;
        }
        return c2 == null ? -1 : Boolean.compare(c2.isSelected(), c1.isSelected());
    };

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
        TABLE_COLUMNS_SORTING.put("shelfColumn", STRING_NUMBER_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("nameColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("authorColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("cycleColumn", NON_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("pagesColumn", NON_COMPARATOR);
    }

    @FXML
    private AnchorPane mainStage;

    @FXML
    private TextField searchQueryField;

    @FXML
    private TableView<BookDTO> bookTableView;

    @FXML
    private TableColumn<BookDTO, String> cycleColumn;

    @FXML
    private TableColumn<BookDTO, Integer> pagesColumn;

    private boolean cycleColumnSortedByASC = true;
    private boolean pagesColumnSortedByASC = true;

    private Stage latestOpenedBookViewStage;
    private int tableFont = MIN_FONT;

    public MainViewController() {
        super("Book library", "view/main-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        TableView<BookDTO> bookTableView = (TableView<BookDTO>) loader.getNamespace().get("bookTableView");
        initTableColumns(loader, bookTableView);
        bookTableView.setItems(observableArrayList(
            BOOK_REPOSITORY.getAll()
                .stream()
                .map(BOOK_CONVERTER::convert)
                .sorted(Comparator.comparing(BookDTO::getName))
                .collect(toList())
            )
        );
        bookTableView.addEventFilter(ScrollEvent.ANY, scrollEvent -> updateTableScale(scrollEvent, bookTableView));
        addSearchTooltip(loader);
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

        // Add numeric for table raw's
        TableColumn<BookDTO, Number> numberColumn = (TableColumn<BookDTO, Number>) loader.getNamespace().get("numberColumn");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(bookTableView.getItems().indexOf(column.getValue()) + 1));
    }

    private void updateTableScale(ScrollEvent scrollEvent, TableView<BookDTO> bookTableView) {
        if (scrollEvent.isControlDown()) {
            if (scrollEvent.getDeltaY() >= 0) {
                if (tableFont < MAX_FONT) {
                    tableFont += 1;
                }
            } else {
                if (tableFont > MIN_FONT) {
                    tableFont -= 1;
                }
            }
            bookTableView.setStyle(format(TABLE_SCALE_PATTERN, tableFont));
        }
    }

    //TODO: переделать на кнопку с описанием
    private void addSearchTooltip(FXMLLoader loader) {
        ImageView signHelp = (ImageView) loader.getNamespace().get("signHelp");
        Tooltip tooltip = new Tooltip(SEARCH_SERVICE_V2.getGeneratedTooltip());
        tooltip.setAutoHide(false);
        tooltip.setFont(new Font(16f));
        tooltip.setShowDelay(new Duration(500f));

        Tooltip.install(signHelp, tooltip);
    }

    @FXML
    public void onSort() {
        if (!bookTableView.getSortOrder().isEmpty()) {
            if (cycleColumn.equals(bookTableView.getSortOrder().get(0))) {
                cycleColumnSortedByASC = !cycleColumnSortedByASC;
            } else {
                cycleColumnSortedByASC = true;
            }

            if (pagesColumn.equals(bookTableView.getSortOrder().get(0))) {
                pagesColumnSortedByASC = !pagesColumnSortedByASC;
            } else {
                pagesColumnSortedByASC = true;
            }
        }
        bookTableView.getItems().sort(getSortComparator());
    }

    private Integer getBookNumberInCycle(String bookNumber) {
        return isNotBlank(bookNumber)
                ? Integer.parseInt(bookNumber.split("/")[0])
                : null;
    }

    @FXML
    public void addBook() {
        loadModalView(
            "Add new book",
            "view/add-view.fxml",
            mainStage,
            null,
            this,
            300,
            -25
        );
//        updateTableColumns(this.bookTableView);
    }

    @FXML
    public void clickOnTable(MouseEvent mouseEvent) {
        if (isDoubleClick(mouseEvent)) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();
            if (clickedEntity != null) {
                String entityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);

                if (this.latestOpenedBookViewStage != null) {
                    this.latestOpenedBookViewStage.close();
                }

                loadBookView(this.mainStage, entityKey, this);
//                updateTableColumns();
            }
        }
    }

    private boolean isDoubleClick(MouseEvent mouseEvent) {
        return MouseButton.PRIMARY.equals(mouseEvent.getButton()) && mouseEvent.getClickCount() >= 2;
    }

    private void loadBookView(AnchorPane mainStage, String initEntityKey, AbstractViewController parentViewController) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + "view/info-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage() {
                @Override
                public void hide() {
                    updateTableColumns();
                    super.hide();
                }
            };

            stage.setResizable(false);
            stage.setTitle("Book info");
            stage.getIcons().add(iconImage);
            stage.setScene(scene);
            stage.initModality(NONE);

            Stage primaryStage = (Stage) mainStage.getScene().getWindow();
            stage.initOwner(primaryStage);

            stage.setX(resolveCoordinate(primaryStage, Stage::getX));
            stage.setY(resolveCoordinate(primaryStage, Stage::getY));

            if (isNotBlank(initEntityKey)) {
                AbstractViewController controller = loader.getController();
                controller.initController(parentViewController, initEntityKey);
            }

            stage.show();
            latestOpenedBookViewStage = stage;

        } catch (Exception e) {
            LOGGER.error("Exception while load model view", e);
        }
    }

    private double resolveCoordinate(Stage primaryStage, Function<Stage, Double> getStageCoordinate) {
        return latestOpenedBookViewStage != null
            ? getStageCoordinate.apply(latestOpenedBookViewStage)
            : getStageCoordinate.apply(primaryStage);
    }

    @FXML
    public void getBooksPrice() {
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
    public void tableViewKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();

            if (clickedEntity != null) {
                Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Вы уверены?",
                        "Книга будет безвозвратно удалена из библиотеки");

                if (answer.isPresent() && OK.equals(answer.get().getText())) {
                    String deletedEntityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);
                    Book deletedEntity = BOOK_REPOSITORY.getByName(deletedEntityKey);
                    BOOK_REPOSITORY.delete(deletedEntity);
                    updateTableColumns();
                }
            }

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Закрыть приложение?", "");

            if (answer.isPresent() && OK.equals(answer.get().getText())) {
                System.exit(0);
            }
        }
    }

    @FXML
    private void searchLabelKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            search();
        }
    }

    //todo: price <= 100 обновляет список после просмотра карточки, нужно починить
    @FXML
    private void search() {
        updateTableColumns();
    }

    //todo: нужно продумать работу сортировки и поиска, чтобы при обновлении страницы не терялись фильтры поиска и сортировки, если они есть
    private void updateTableColumns() {
        System.out.println("SELECTED: " + bookTableView.getSelectionModel().getSelectedItem());
        System.out.println("FOCUSED: " + bookTableView.getFocusModel().getFocusedItem());
        System.out.println("-----------");

        String searchQuery = ofNullable(searchQueryField)
            .map(TextInputControl::getText)
            .orElse(null);

        Comparator<BookDTO> booksComparator = getSortComparator();
        String focusedBookKey = ofNullable(bookTableView.getFocusModel())
            .map(FocusModel::getFocusedItem)
            .map(BOOK_CONVERTER::buildEntityKeyByDTO)
            .orElse(null);

        var focusedBook = ofNullable(bookTableView.getFocusModel())
            .map(FocusModel::getFocusedItem)
//            .map(BOOK_CONVERTER::buildEntityKeyByDTO)
            .orElse(null);

        List<BookDTO> bookDTOListWithFiltersAndSorting = SEARCH_SERVICE_V2.search(searchQuery, getDtoBooks())
            .stream()
            .sorted(booksComparator)
            .toList();

        int focusRowIndex = bookDTOListWithFiltersAndSorting.stream()
            .map(BOOK_CONVERTER::buildEntityKeyByDTO)
            .toList()
            .indexOf(focusedBookKey);

//        bookTableView.getItems().removeAll();
//        bookTableView.getItems().addAll(bookDTOListWithFiltersAndSorting);
        bookTableView.setItems(observableArrayList(bookDTOListWithFiltersAndSorting));
//        bookTableView.getFocusModel().focus(focusRowIndex);
//        bookTableView.getSelectionModel().select(focusRowIndex);

//        System.out.println("FOCUS INDEX = " + focusRowIndex);
//        System.out.println("ITEMS INDEX = " + bookTableView.getItems().indexOf(focusedBook));


        System.out.println("SELECTED: " + bookTableView.getSelectionModel().getSelectedItem());
        System.out.println("FOCUSED: " + bookTableView.getFocusModel().getFocusedItem());
        System.out.println("============\n");



//        bookTableView.setItems(observableArrayList(
//            getDtoBooks().stream()
//                .sorted(Comparator.comparing(BookDTO::getName))
//                .toList()
//            )
//        );

//        if (StringUtils.isNotBlank(this.cursorBookKey)) {
//            bookTableView.getSelectionModel().focus(
//                bookTableView.getItems()
//                    .stream()
//                    .map(BOOK_CONVERTER::buildEntityKeyByDTO)
//                    .toList()
//                    .indexOf(cursorBookKey)
//            );
//        }
    }

    private List<BookDTO> getDtoBooks() {
        return BOOK_REPOSITORY.getAll()
            .stream()
            .map(BOOK_CONVERTER::convert)
            .collect(toList());
    }

    private Comparator<BookDTO> getSortComparator() {
        if (bookTableView.getSortOrder().isEmpty()) {
            // Nothing chosen. Sort by book name by default
            return comparing(BookDTO::getName);
        }

        // Sort by cycle name + book number in cycle
        if (cycleColumn.equals(bookTableView.getSortOrder().get(0))) {
            if (cycleColumnSortedByASC) {
                return comparing(BookDTO::getCycleName, nullsLast(naturalOrder()))
                    .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle);
            }

            return comparing(BookDTO::getCycleName, nullsFirst(naturalOrder()))
                .reversed()
                .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle);
        }

        // Sort by pages + !read
        if (pagesColumn.equals(bookTableView.getSortOrder().get(0))) {
            if (pagesColumnSortedByASC) {
                return comparing(BookDTO::getRead,
                    (c1, c2) -> Boolean.compare(c1.isSelected(), c2.isSelected()))
                    .thenComparing(BookDTO::getPages);
            }

            return comparing(BookDTO::getRead, (c1, c2) -> Boolean.compare(c2.isSelected(), c1.isSelected()))
                .thenComparing(BookDTO::getPages)
                .reversed();
        }

        return comparing(BookDTO::getName);
    }

    private int comparatorByBookNumberInCycle(String bookNumber1, String bookNumber2) {
        Integer number1 = getBookNumberInCycle(bookNumber1);
        Integer number2 = getBookNumberInCycle(bookNumber2);

        if (number1 == null) {
            return number2 == null ? 0 : -1;
        }

        return number2 == null ? 1 : Integer.compare(number1, number2);
    }
}
