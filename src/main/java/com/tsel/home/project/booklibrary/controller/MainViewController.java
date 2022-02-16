package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.SearchService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Function;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.stage.Modality.NONE;

public class MainViewController extends AbstractViewController {

    private static final SearchService SEARCH_SERVICE = new SearchService();

    private static final Comparator<CheckBox> CHECK_BOX_COMPARATOR =
            (checkBox1, checkBox2) -> Boolean.compare(checkBox2.isSelected(), checkBox1.isSelected());

    private static final Comparator<String> STRING_NUMBER_COMPARATOR =
            (strValue1, strValue2) -> Integer.compare(Integer.parseInt(strValue2), Integer.parseInt(strValue1));

    private static final Map<String, String> TABLE_COLUMNS_SETTINGS;
    private static final Map<String, Comparator> TABLE_COLUMNS_SORTING;

    static {
        TABLE_COLUMNS_SETTINGS = new HashMap<>();
        TABLE_COLUMNS_SETTINGS.put("nameColumn", "name");
        TABLE_COLUMNS_SETTINGS.put("authorColumn", "author");
        TABLE_COLUMNS_SETTINGS.put("shelfColumn", "shelf");
        TABLE_COLUMNS_SETTINGS.put("cycleColumn", "cycleName");
        TABLE_COLUMNS_SETTINGS.put("cycleNumberColumn", "cycleNumber");
        TABLE_COLUMNS_SETTINGS.put("readColumn", "read");

        TABLE_COLUMNS_SORTING = new HashMap<>();
        TABLE_COLUMNS_SORTING.put("readColumn", CHECK_BOX_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("shelfColumn", STRING_NUMBER_COMPARATOR);
        TABLE_COLUMNS_SORTING.put("nameColumn", String.CASE_INSENSITIVE_ORDER);
        TABLE_COLUMNS_SORTING.put("authorColumn", String.CASE_INSENSITIVE_ORDER);
    }

    @FXML
    private AnchorPane mainStage;

    @FXML
    private TextField searchQueryField;

    @FXML
    private TableView<BookDTO> bookTableView;

    @FXML
    private TableColumn<BookDTO, String> cycleColumn;
    private boolean cycleColumnSortedByASC = false;

    private Stage lastOpenedBookViewStage;

    public MainViewController() {
        super("Book library", "main-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        initTableColumns(loader);
        TableView<BookDTO> bookTableView = (TableView<BookDTO>) loader.getNamespace().get("bookTableView");
        updateTableColumns(bookTableView);
        bookTableView.getItems().sort(comparing(BookDTO::getName));
    }

    @SuppressWarnings("unchecked")
    private void initTableColumns(FXMLLoader loader) {
        TABLE_COLUMNS_SETTINGS.forEach((columnName, fieldName) -> {
            TableColumn<BookDTO, ?> column = (TableColumn<BookDTO, ?>) loader.getNamespace().get(columnName);
            column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
            column.setComparator((o1, o2) -> 0);

            if (TABLE_COLUMNS_SORTING.containsKey(columnName)) {
                column.setComparator(TABLE_COLUMNS_SORTING.get(columnName));
            }
        });
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
                    bookTableView.getItems().sort(comparing(BookDTO::getCycleName, nullsLast(naturalOrder())).reversed()
                            .thenComparing(this::comparatorByBookNumberInCycle));
                } else {
                    bookTableView.getItems().sort(comparing(BookDTO::getCycleName, nullsLast(naturalOrder()))
                            .thenComparing(this::comparatorByBookNumberInCycle));
                }

                cycleColumnSortedByASC = !cycleColumnSortedByASC;
            } else {
                cycleColumnSortedByASC = false;
            }
        }
    }

    private int comparatorByBookNumberInCycle(BookDTO b1, BookDTO b2) {
        Integer b1Number = getBookNumberInCycle(b1);
        Integer b2Number = getBookNumberInCycle(b2);

        if (b1Number == null) {
            return b2Number == null ? 0 : -1;
        }

        return b2Number == null ? 1 : Integer.compare(b1Number, b2Number);
    }

    private Integer getBookNumberInCycle(BookDTO bookDTO) {
        return isNotBlank(bookDTO.getCycleNumber())
                ? Integer.parseInt(bookDTO.getCycleNumber().split("/")[0])
                : null;
    }

    @FXML
    public void addBook() {
        loadModalView("Add new book", "add-view.fxml", mainStage, null,
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
                Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Вы уверены?",
                        "Книга будет безвозратно удалена из библиотеки");

                if (answer.isPresent() && OK.equals(answer.get().getText())) {
                    String deletedEntityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);
                    Book deletedEntity = BOOK_REPOSITORY.getByName(deletedEntityKey);
                    BOOK_REPOSITORY.delete(deletedEntity);
                    updateTableColumns(bookTableView);
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

    @FXML
    private void search() {
        String searchQuery = searchQueryField.getText();
        bookTableView.setItems(observableArrayList(SEARCH_SERVICE.search(searchQuery, getDtoBooks())));
    }

    private void updateTableColumns(TableView<BookDTO> bookTableView) {
        bookTableView.setItems(observableArrayList(getDtoBooks()));
    }

    private List<BookDTO> getDtoBooks() {
        return BOOK_REPOSITORY.getAll()
                .stream()
                .map(BOOK_CONVERTER::convert)
                .collect(toList());
    }

    protected void loadBookView(AnchorPane mainStage, String initEntityKey, AbstractViewController parentViewController) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + "info-view.fxml"));
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
}
