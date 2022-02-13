package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.SearchService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

public class MainViewController extends AbstractViewController {

    private static final SearchService SEARCH_SERVICE = new SearchService();

    private static final Map<String, String> TABLE_COLUMNS_SETTINGS;

    static {
        TABLE_COLUMNS_SETTINGS = new HashMap<>();
        TABLE_COLUMNS_SETTINGS.put("nameColumn", "name");
        TABLE_COLUMNS_SETTINGS.put("authorColumn", "author");
        TABLE_COLUMNS_SETTINGS.put("shelfColumn", "shelf");
        TABLE_COLUMNS_SETTINGS.put("cycleColumn", "cycleName");
        TABLE_COLUMNS_SETTINGS.put("cycleNumberColumn", "cycleNumber");
        TABLE_COLUMNS_SETTINGS.put("readColumn", "read");
    }

    @FXML
    private AnchorPane mainStage;

    @FXML
    private TextField searchQueryField;

    @FXML
    private TableView<BookDTO> bookTableView;

    public MainViewController() {
        super("Book library", "main-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        initTableColumns(loader);
        TableView<BookDTO> tableView = (TableView<BookDTO>) loader.getNamespace().get("bookTableView");
        updateTableColumns(tableView);
    }

    @SuppressWarnings("rawtypes")
    private void initTableColumns(FXMLLoader loader) {
        TABLE_COLUMNS_SETTINGS.forEach((columnName, fieldName) -> {
            TableColumn<?, ?> column = (TableColumn) loader.getNamespace().get(columnName);
            column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
        });
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
                loadModalView("Book info", "info-view.fxml", mainStage, entityKey,
                        this, 100, 0);
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

    private void updateTableColumns(TableView<BookDTO> tableView) {
        tableView.setItems(observableArrayList(getDtoBooks()));
    }

    private List<BookDTO> getDtoBooks() {
        return BOOK_REPOSITORY.getAll()
                .stream()
                .map(BOOK_CONVERTER::convert)
                .collect(toList());
    }
}
