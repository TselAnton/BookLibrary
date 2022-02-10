package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

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
        bookShelfNumberInput.setText(String.valueOf(book.getBookshelf()));
        imageInput.setText(book.getCoverImgAbsolutePath());

        isReadCheckBox.setSelected(TRUE.equals(book.getRead()));

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
        addBook(nameInput, authorInput, publisherInput, pagesCountInput, isReadCheckBox, bookShelfNumberInput,
                cycleInput, isEndedCycleCheckBox, numberInCycleInput, totalCountInCycleInput, imageInput, editButton);
    }

    @Override
    protected boolean doUpdate(Book newBook, Author newAuthor, Publisher newPublisher, Cycle newCycle) {
        if (!book.getKey().equals(newBook.getKey())) {
            BOOK_REPOSITORY.delete(book);
        }
        BOOK_REPOSITORY.save(newBook);

        if (!book.getAuthor().equals(newBook.getAuthor())) {
            if (isValueNotLinked(book.getAuthor(), Book::getAuthor)) {
                AUTHOR_REPOSITORY.delete(new Author(book.getAuthor()));
            }
        }
        AUTHOR_REPOSITORY.save(newAuthor);

        if (!book.getPublisher().equals(newBook.getPublisher())) {
            if (isValueNotLinked(book.getPublisher(), Book::getPublisher)) {
                PUBLISHER_REPOSITORY.delete(new Publisher(book.getPublisher()));
            }
        }
        PUBLISHER_REPOSITORY.save(newPublisher);

        if (isNotBlank(book.getCycleName()) && isDifferentCycleName(book, newBook)) {
            if (isValueNotLinked(book.getCycleName(), Book::getCycleName)) {
                Cycle oldCycle = CYCLE_REPOSITORY.getByName(book.getCycleName());
                CYCLE_REPOSITORY.delete(oldCycle);
            }
        }

        if (newCycle != null) {
            CYCLE_REPOSITORY.save(newCycle);
        }

        parentController.updateControllerState(newBook.getKey());
        return true;
    }

    private boolean isValueNotLinked(String fieldValue, Function<Book, String> mapFunc) {
        return BOOK_REPOSITORY.getAll()
                .stream()
                .map(mapFunc)
                .filter(StringUtils::isNotBlank)
                .noneMatch(value -> value.equals(fieldValue));
    }

    private boolean isDifferentCycleName(Book oldBook, Book newBook) {
        String oldCycleName = oldBook.getCycleName();
        String newCycleName = newBook.getCycleName();

        return !oldCycleName.equals(newCycleName);
    }
}
