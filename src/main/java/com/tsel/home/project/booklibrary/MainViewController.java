package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.builder.BookDTOBuilder;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class MainViewController extends Application {

    private final BookRepository bookRepository = BookRepository.getInstance();

    private final CycleRepository cycleRepository = CycleRepository.getInstance();

    private Image iconImage = null;

    @FXML
    private AnchorPane mainStage;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<BookDTO> bookTable;

    @FXML
    private TableColumn<BookDTO, String> nameColumn;

    public MainViewController() {
        try {
            iconImage = new Image(requireNonNull(MainViewController.class.getResourceAsStream("icon.png")));
        } catch (Exception e) {
            e.printStackTrace();
            //todo: logger it
        }
    }

    @FXML
    @Override
    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader mainWindowLoader = new FXMLLoader(MainViewController.class.getResource("main-view.fxml"));
        Scene scene = new Scene(mainWindowLoader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Book library");
        primaryStage.getIcons().add(iconImage);
        primaryStage.setScene(scene);

        initTableColumns(mainWindowLoader);
        TableView<BookDTO> tableView = (TableView<BookDTO>) mainWindowLoader.getNamespace().get("bookTable");
        updateTableColumns(tableView);

        primaryStage.show();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initTableColumns(FXMLLoader loader) {
        TableColumn<BookDTO, String> nameColumn = (TableColumn) loader.getNamespace().get("nameColumn");
        TableColumn<BookDTO, String> authorColumn = (TableColumn) loader.getNamespace().get("authorColumn");
        TableColumn<BookDTO, String> shelfColumn = (TableColumn) loader.getNamespace().get("shelfColumn");
        TableColumn<BookDTO, String> cycleColumn = (TableColumn) loader.getNamespace().get("cycleColumn");
        TableColumn<BookDTO, String> cycleNumberColumn = (TableColumn) loader.getNamespace().get("cycleNumberColumn");
        TableColumn<BookDTO, String> readColumn = (TableColumn) loader.getNamespace().get("readColumn");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        shelfColumn.setCellValueFactory(new PropertyValueFactory<>("shelf"));
        cycleColumn.setCellValueFactory(new PropertyValueFactory<>("cycleName"));
        cycleNumberColumn.setCellValueFactory(new PropertyValueFactory<>("cycleNumber"));
        readColumn.setCellValueFactory(new PropertyValueFactory<>("read"));
    }

    @FXML
    public void addBook(ActionEvent actionEvent) throws IOException {
        showModalView("add-view.fxml", "Add new book");

        //todo: update info
    }

    @FXML
    public void editBook(ActionEvent actionEvent) throws IOException {
        showModalView("edit-view.fxml", "Edit book");

        //todo: update info
    }

    private void showModalView(String modalViewName, String title) throws IOException {
        FXMLLoader modalViewLoader = new FXMLLoader(MainViewController.class.getResource(modalViewName));
        Scene secondScene = new Scene(modalViewLoader.load());

        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.getIcons().add(iconImage);
        newWindow.setScene(secondScene);
        newWindow.setResizable(false);
        newWindow.initModality(Modality.WINDOW_MODAL);

        Stage primaryStage = (Stage) mainStage.getScene().getWindow();
        newWindow.initOwner(primaryStage);

        newWindow.setX(primaryStage.getX() + 300);
        newWindow.setY(primaryStage.getY() - 25);

        newWindow.show();
    }

    private void updateTableColumns(TableView<BookDTO> tableView) {

        List<BookDTO> bookDTOS = bookRepository.getAll()
                .stream()
                .map(this::convert)
                .collect(toList());

        tableView.setItems(FXCollections.observableArrayList(bookDTOS));
    }

    private BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = cycleRepository.getByName(book.getCycleName());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
        }

        return BookDTOBuilder.builder()
                .name(book.getName())
                .author(book.getAuthor())
                .shelf(String.valueOf(book.getBookshelf()))
                .cycleName(cycleName)
                .cycleNumber(cycleNumber)
                .build();
    }
}
