package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class EditBookViewController extends AbstractEditViewController {

    @FXML
    private TextField nameInput;

    @FXML
    private ComboBox<String> authorInput;

    @FXML
    private ComboBox<String> publisherInput;

    @FXML
    private TextField pagesCountInput;

    @FXML
    private CheckBox isReadCheckBox;

    @FXML
    private CheckBox isAutographCheckBox;

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
    private Button editButton;

    private Book book;
    private AbstractViewController parentController;

    @Override
    public void initController(AbstractViewController parentController, String entityKey) {
        init(authorInput, publisherInput, cycleInput, isEndedCycleCheckBox, totalCountInCycleInput);

        if (isBlank(entityKey)) {
            throw new IllegalStateException("Entity key for edit controller is blank!");
        }

        Book book = BOOK_REPOSITORY.getByName(entityKey);
        if (book == null) {
            throw new IllegalStateException(format("Book by key = \"%s\" not found for edit controller!", entityKey));
        }

        this.book = book;
        this.parentController = parentController;

        nameInput.setText(book.getName());
        pagesCountInput.setText(String.valueOf(book.getPages()));
        imageInput.setText(book.getCoverImgAbsolutePath());

        isReadCheckBox.setSelected(TRUE.equals(book.getRead()));
        isAutographCheckBox.setSelected(TRUE.equals(book.getAutograph()));

        initComboBoxValue(authorInput, book::getAuthor);
        initComboBoxValue(publisherInput, book::getPublisher);
        initComboBoxValue(cycleInput, book::getCycleName);

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = CYCLE_REPOSITORY.getByName(book.getCycleName());

            isEndedCycleCheckBox.setSelected(TRUE.equals(cycle.getEnded()));
            numberInCycleInput.setText(String.valueOf(book.getNumberInSeries()));
            totalCountInCycleInput.setText(String.valueOf(cycle.getBooksInCycle()));
        }
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
            editBook();
        }
    }

    private void initComboBoxValue(ComboBox<String> comboBox, Supplier<String> consumer) {
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            if (comboBox.getItems().get(i).equals(consumer.get())) {
                comboBox.getSelectionModel().select(i);
                return;
            }
        }
    }

    @FXML
    public void cancelEditing() {
        closeStage(cancelButton);
    }

    @FXML
    public void chooseBookCover() {
        selectFileAsCover(selectFileButton, imageInput);
    }

    @FXML
    public void editBook() {
        addBook(
            nameInput,
            authorInput,
            publisherInput,
            pagesCountInput,
            isReadCheckBox,
            isAutographCheckBox,
            cycleInput,
            isEndedCycleCheckBox,
            numberInCycleInput,
            totalCountInCycleInput,
            imageInput,
            editButton
        );
    }

    @Override
    protected boolean doUpdate(Book newBook, Author newAuthor, Publisher newPublisher, Cycle newCycle) {
        BOOK_REPOSITORY.delete(book);

        BOOK_REPOSITORY.save(newBook);
        AUTHOR_REPOSITORY.save(newAuthor);
        PUBLISHER_REPOSITORY.save(newPublisher);

        if (newCycle != null) {
            CYCLE_REPOSITORY.save(newCycle);
        }

        parentController.updateControllerState(newBook.getKey());
        return true;
    }
}
