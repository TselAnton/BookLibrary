package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.SearchService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Function;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.stage.Modality.NONE;

public class MainViewController extends AbstractViewController {

    private static final String TABLE_SCALE_PATTERN = "-fx-font-size: %spt";
    private static final int MIN_FONT = 9;
    private static final int MAX_FONT = 16;
    private int tableFont = MIN_FONT;

    private static final SearchService SEARCH_SERVICE = new SearchService();

    private static final Comparator<String> STRING_NUMBER_COMPARATOR = comparingInt(Integer::parseInt);
    private static final Comparator<Object> NON_COMPARATOR = (x1, x2) -> 0;

    private static final Map<String, String> TABLE_COLUMNS_SETTINGS;
    private static final Map<String, Comparator> TABLE_COLUMNS_SORTING;

    static {
        TABLE_COLUMNS_SETTINGS = new HashMap<>();
        TABLE_COLUMNS_SETTINGS.put("nameColumn", "name");
        TABLE_COLUMNS_SETTINGS.put("authorColumn", "author");
        TABLE_COLUMNS_SETTINGS.put("shelfColumn", "shelf");
        TABLE_COLUMNS_SETTINGS.put("cycleColumn", "cycleName");
        TABLE_COLUMNS_SETTINGS.put("cycleNumberColumn", "cycleNumber");
        TABLE_COLUMNS_SETTINGS.put("cycleEndColumn", "cycleEnded");
        TABLE_COLUMNS_SETTINGS.put("readColumn", "read");
        TABLE_COLUMNS_SETTINGS.put("pagesColumn", "pages");

        TABLE_COLUMNS_SORTING = new HashMap<>();
        TABLE_COLUMNS_SORTING.put("readColumn", MainViewController::compareCheckBoxes);
        TABLE_COLUMNS_SORTING.put("cycleEndColumn", MainViewController::compareCheckBoxes);
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
    private boolean cycleColumnSortedByASC = true;

    @FXML
    private TableColumn<BookDTO, Integer> pagesColumn;
    private boolean pagesColumnSortedByASC = true;

    private Stage lastOpenedBookViewStage;

    public MainViewController() {
        super("Book library", "view/main-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        TableView<BookDTO> bookTableView = (TableView<BookDTO>) loader.getNamespace().get("bookTableView");
        initTableColumns(loader, bookTableView);
        updateTableColumns(bookTableView);
        bookTableView.getItems().sort(comparing(BookDTO::getName));

        bookTableView.addEventFilter(ScrollEvent.ANY, scrollEvent -> updateTableScale(scrollEvent, bookTableView));

        addSearchTooltip(loader);
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
        TableColumn<BookDTO, Number> numberColumn =
                (TableColumn<BookDTO, Number>) loader.getNamespace().get("numberColumn");

        numberColumn.setCellValueFactory(
                column-> new ReadOnlyObjectWrapper<>(bookTableView.getItems().indexOf(column.getValue()) + 1));
    }

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
                                .thenComparing(BookDTO::getCycleNumber, this::comparatorByBookNumberInCycle));
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
                                    .thenComparing(BookDTO::getPages));

                } else {
                    bookTableView.getItems()
                            .sort(comparing(BookDTO::getRead, (c1, c2) -> Boolean.compare(c2.isSelected(), c1.isSelected()))
                                    .thenComparing(BookDTO::getPages)
                                    .reversed());
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

    @FXML
    public void addBook() {
        loadModalView("Add new book", "view/add-view.fxml", mainStage, null,
                this, 300, -25);
        updateTableColumns(bookTableView);
    }

    @FXML
    public void clickOnTable(MouseEvent mouseEvent) {
        if (isDoubleClick(mouseEvent)) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();

            if (clickedEntity != null) {
                String entityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);

                if (lastOpenedBookViewStage != null) {
                    lastOpenedBookViewStage.close();
                }

                loadBookView(mainStage, entityKey, this);
                updateTableColumns(bookTableView);
            }
        }
    }

    private boolean isDoubleClick(MouseEvent mouseEvent) {
        return MouseButton.PRIMARY.equals(mouseEvent.getButton()) && mouseEvent.getClickCount() >= 2;
    }

    @FXML
    public void tableViewKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            BookDTO clickedEntity = bookTableView.getSelectionModel().getSelectedItem();

            if (clickedEntity != null) {
                Optional<ButtonType> answer = riseAlert(CONFIRMATION, "????????????????!", "???? ???????????????",
                        "?????????? ?????????? ?????????????????????? ?????????????? ???? ????????????????????");

                if (answer.isPresent() && OK.equals(answer.get().getText())) {
                    String deletedEntityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);
                    Book deletedEntity = BOOK_REPOSITORY.getByName(deletedEntityKey);
                    BOOK_REPOSITORY.delete(deletedEntity);
                    updateTableColumns(bookTableView);
                }
            }

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            Optional<ButtonType> answer = riseAlert(CONFIRMATION, "????????????????!", "?????????????? ?????????????????????", "");

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

    @FXML
    private void search() {
        String searchQuery = searchQueryField.getText();
        bookTableView.setItems(observableArrayList(SEARCH_SERVICE.search(searchQuery, getDtoBooks())));
    }

    private void addSearchTooltip(FXMLLoader loader) {
        ImageView signHelp = (ImageView) loader.getNamespace().get("signHelp");
        Tooltip tooltip = new Tooltip(
                "?????????? ???? ??????????????????????: read / nread\n" +
                        "?????????? ???? ?????????????????????? ????????????: end / nend"
        );
        tooltip.setAutoHide(false);
        tooltip.setFont(new Font(16f));
        tooltip.setShowDelay(new Duration(500f));

        Tooltip.install(signHelp, tooltip);
    }

    private void updateTableColumns(TableView<BookDTO> bookTableView) {
        bookTableView.setItems(observableArrayList(
                getDtoBooks().stream()
                        .sorted(Comparator.comparing(BookDTO::getName))
                        .toList()
                ));
    }

    private List<BookDTO> getDtoBooks() {
        return BOOK_REPOSITORY.getAll()
                .stream()
                .map(BOOK_CONVERTER::convert)
                .collect(toList());
    }

    protected void loadBookView(AnchorPane mainStage, String initEntityKey, AbstractViewController parentViewController) {
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
            lastOpenedBookViewStage = stage;

        } catch (Exception e) {
            LOGGER.error("Exception while load model view", e);
        }
    }

    private double resolveCoordinate(Stage primaryStage, Function<Stage, Double> getStageCoordinate) {
        return lastOpenedBookViewStage != null
                ? getStageCoordinate.apply(lastOpenedBookViewStage)
                : getStageCoordinate.apply(primaryStage);
    }

    private static int compareCheckBoxes(Object c1, Object c2) {
        if (c1 == null) {
            return c2 == null ? 0 : 1;
        }
        return c2 == null ? -1 : Boolean.compare(((CheckBox) c2).isSelected(), ((CheckBox) c1).isSelected());
    }
}
