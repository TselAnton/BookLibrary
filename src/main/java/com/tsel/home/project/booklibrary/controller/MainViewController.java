package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;

public class MainViewController extends AbstractViewController {

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
    private Button searchButton;

    @FXML
    private TableView<BookDTO> bookTable;

    public MainViewController() {
        super("Book library", "main-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        initTableColumns(loader);
        TableView<BookDTO> tableView = (TableView<BookDTO>) loader.getNamespace().get("bookTable");
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
        updateTableColumns(bookTable);
    }

    @FXML
    public void clickOnTable(MouseEvent mouseEvent) {
        if (isDoubleClick(mouseEvent)) {
            BookDTO clickedEntity = bookTable.getSelectionModel().getSelectedItem();
            String entityKey = BOOK_CONVERTER.buildEntityKeyByDTO(clickedEntity);

            loadModalView("Book info", "info-view.fxml", mainStage, entityKey,
                    this,  100, 0);
            updateTableColumns(bookTable);
        }
    }

    private boolean isDoubleClick(MouseEvent mouseEvent) {
        return MouseButton.PRIMARY.equals(mouseEvent.getButton()) && mouseEvent.getClickCount() >= 2;
    }

    private void updateTableColumns(TableView<BookDTO> tableView) {
        List<BookDTO> bookDTOS = BOOK_REPOSITORY.getAll()
                .stream()
                .map(BOOK_CONVERTER::convert)
                .collect(toList());

        tableView.setItems(observableArrayList(bookDTOS));
    }
}
