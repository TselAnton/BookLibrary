package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.builder.BookBuilder;
import com.tsel.home.project.booklibrary.builder.CycleBuilder;
import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.repository.impl.PublisherRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

public class AddBookViewController {

    private final BookRepository bookRepository = BookRepository.getInstance();

    private final CycleRepository cycleRepository = CycleRepository.getInstance();

    private final AuthorRepository authorRepository = AuthorRepository.getInstance();

    private final PublisherRepository publisherRepository = PublisherRepository.getInstance();

    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private TextField nameInput;

    @FXML
    private ComboBox<String> authorInput;

    @FXML
    private ComboBox<String> publisherInput;

    @FXML
    private TextField pagesCountInput;

    @FXML
    private TextField bookShelfNumberInput;

    @FXML
    private CheckBox isReadCheckBox;

    @FXML
    private CheckBox isEndedCycleCheckBox;

    @FXML
    private ComboBox<String> cycleInput;

    @FXML
    private TextField imageInput;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextField numberInCycleInput;

    @FXML
    private TextField totalCountInCycleInput;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addButton;

    @FXML
    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        fileChooser.setTitle("Выбрать обложку книги");

        authorInput.setItems(FXCollections.observableArrayList(
                authorRepository.getAll().stream()
                        .map(Author::getName)
                        .collect(toList())
        ));

        publisherInput.setItems(FXCollections.observableArrayList(
                publisherRepository.getAll().stream()
                        .map(Publisher::getName)
                        .collect(toList())
        ));

        cycleInput.setItems(FXCollections.observableArrayList(
                cycleRepository.getAll().stream()
                        .map(Cycle::getName)
                        .collect(toList())
        ));

        cycleInput.setOnAction(event -> {
            String cycleName = getInputText(cycleInput);
            if (isNotBlank(cycleName)) {
                Cycle cycle = cycleRepository.getByName(cycleName);
                if (cycle != null) {
                    isEndedCycleCheckBox.setSelected(TRUE.equals(cycle.getEnded()));
                    totalCountInCycleInput.setText(String.valueOf(cycle.getBooksInCycle()));
                }
            }
        });
    }

    @FXML
    public void chooseBookCover(ActionEvent actionEvent) {
        Stage stage = (Stage) selectFileButton.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            imageInput.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void addBook(ActionEvent actionEvent) {
        String bookName = getInputText(nameInput);
        String author = getInputText(authorInput);
        String publisher = getInputText(publisherInput);
        String pages = getInputText(pagesCountInput);
        boolean isRead = isChecked(isReadCheckBox);

        String shelfNumber = getInputText(bookShelfNumberInput);

        String cycle = getInputText(cycleInput);
        boolean isEndedCycle = isChecked(isEndedCycleCheckBox);

        String numberInCycle = getInputText(numberInCycleInput);
        String totalInCycle = getInputText(totalCountInCycleInput);

        String imagePath = getInputText(imageInput);

        if (bookName == null) {
            riseWarnMsg("Название книги не заполнено", "Название книги не может быть пустым");
            return;
        }
        if (author == null) {
            riseWarnMsg("Имя автора не заполнено", "Имя автора не может быть пустым");
            return;
        }
        if (publisher == null) {
            riseWarnMsg("Название издателя не заполнено", "Название издателя не может быть пустым");
            return;
        }
        if (pages == null) {
            riseWarnMsg("Количество страниц не заполнено", "Количество страниц в книги не может быть пустым");
            return;
        }
        if (stringToInt(pages) == null) {
            riseWarnMsg("Неверно заполнено количество страниц", "Количество страниц должно быть записано в виде целого числа");
            return;
        }
        if (isNotBlank(shelfNumber) && stringToInt(shelfNumber) == null) {
            riseWarnMsg("Неверно заполнен номер полки", "Номер полки должен быть записан в виде целого числа");
            return;
        }
        if (isNotBlank(numberInCycle) && stringToInt(numberInCycle) == null) {
            riseWarnMsg("Неверно заполнен номер книги в цикле", "Номер книги в цикле должен быть записан в виде целого числа");
            return;
        }
        if (isNotBlank(totalInCycle) && stringToInt(totalInCycle) == null) {
            riseWarnMsg("Неверно заполнено количество книги в цикле", "Количество книг в цикле должно быть записано в виде целого числа");
            return;
        }
        if (isBlank(cycle) && isEndedCycle) {
            riseWarnMsg("Название цикла не заполнено", "Указано, что цикл окончен, но название цикла при этом не может быть пустым");
            return;
        }
        if (isBlank(cycle) && isNotBlank(numberInCycle)) {
            riseWarnMsg("Название цикла не заполнено", "Указан номер книги в цикле, но название цикла при этом не может быть пустым");
            return;
        }
        if (isBlank(cycle) && isNotBlank(totalInCycle)) {
            riseWarnMsg("Название цикла не заполнено", "Указано количество книг в цикле, но название цикла при этом не может быть пустым");
            return;
        }

        if (isNotBlank(cycle)) {
            Cycle newCycle = CycleBuilder.builder()
                    .name(cycle)
                    .ended(isEndedCycle)
                    .booksInCycle(stringToInt(totalInCycle))
                    .build();

            cycleRepository.save(newCycle);
        }

        authorRepository.save(new Author(author));
        publisherRepository.save(new Publisher(publisher));

        Book newBook = BookBuilder.builder()
                .name(bookName)
                .author(author)
                .publisher(publisher)
                .pages(stringToInt(pages))
                .bookshelf(stringToInt(shelfNumber))
                .read(isRead)
                .cycleName(cycle)
                .numberInSeries(stringToInt(numberInCycle))
                .coverImgAbsolutePath(imagePath)
                .build();

        bookRepository.save(newBook);

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    private Integer stringToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getInputText(TextField textField) {
        String textFieldText = textField.getText();
        return isNotBlank(textFieldText) ? textFieldText : null;
    }

    private String getInputText(ComboBox<String> comboBox) {
        String comboBoxText = comboBox.getSelectionModel().getSelectedItem();
        return isNotBlank(comboBoxText) ? comboBoxText : null;
    }

    private boolean isChecked(CheckBox checkBox) {
        return TRUE.equals(checkBox.isSelected());
    }

    private void riseWarnMsg(String warnMsg, String explanationMsg) {
        Alert errorAlert = new Alert(Alert.AlertType.WARNING);
        errorAlert.setTitle("Ошибка");
        errorAlert.setHeaderText(warnMsg);
        errorAlert.setContentText(explanationMsg);
        errorAlert.showAndWait();
    }
}
