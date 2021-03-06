package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.utils.AutoCompleteComboBoxListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class AddBookViewController extends AbstractEditViewController {

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
    public void initialize() {
        init(authorInput, publisherInput, cycleInput, isEndedCycleCheckBox, totalCountInCycleInput);
    }

    @FXML
    public void cancelAdding() {
        closeStage(cancelButton);
    }

    @FXML
    public void chooseBookCover() {
        selectFileAsCover(selectFileButton, imageInput);
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Закрыть окно?",
                    "Все несохранённые данные будут потеряны");

            if (answer.isPresent() && OK.equals(answer.get().getText())) {
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
            }
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            addBook();
        }
    }

    @FXML
    public void addBook() {
        addBook(nameInput, authorInput, publisherInput, pagesCountInput, isReadCheckBox, bookShelfNumberInput,
                cycleInput, isEndedCycleCheckBox, numberInCycleInput, totalCountInCycleInput, imageInput, addButton);
    }

    @Override
    protected boolean doUpdate(Book newBook, Author newAuthor, Publisher newPublisher, Cycle newCycle) {
        if (BOOK_REPOSITORY.getByName(newBook.getKey()) != null) {
            riseAlert(WARNING, "Ошибка", "Такая книга уже существует",
                    "Книга с аналогичным названием, автором и номером цикла уже существует");
            return false;
        }

        if (newCycle != null) {
            CYCLE_REPOSITORY.save(newCycle);
        }

        AUTHOR_REPOSITORY.save(newAuthor);
        PUBLISHER_REPOSITORY.save(newPublisher);
        BOOK_REPOSITORY.save(newBook);

        return true;
    }
}
