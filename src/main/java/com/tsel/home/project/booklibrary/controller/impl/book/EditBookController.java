package com.tsel.home.project.booklibrary.controller.impl.book;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import com.tsel.home.project.booklibrary.utils.elements.ButtonAnswer;
import java.util.Objects;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class EditBookController extends AbstractBookController {

    private static final Logger log = LogManager.getLogger(EditBookController.class);

    // Основные элементы

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private Button exitButton;

    @FXML
    private Button saveBookButton;

    // Элементы ввода

    @FXML
    private TextField nameTextFieldInput;

    @FXML
    private TextField pagesCountFieldInput;

    @FXML
    private TextField priceFieldInput;

    @FXML
    private CheckBox readCheckBox;

    @FXML
    private CheckBox autographCheckBox;

    @FXML
    private CheckBox hardCoverCheckBox;

    // ComboBoxes для выбора

    @FXML
    private ComboBox<ComboBoxDTO> authorComboBox;

    @FXML
    private ComboBox<ComboBoxDTO> publisherComboBox;

    // Настройки цикла

    @FXML
    private ComboBox<ComboBoxDTO> cycleComboBox;

    @FXML
    private CheckBox cycleEndedCheckBox;

    @FXML
    private TextField numberInCycleFieldInput;

    @FXML
    private TextField totalCountInCycleFieldInput;

    // Настройки обложки

    @FXML
    private TextField coverImagePathFieldInput;

    @FXML
    private Button selectCoverFileButton;

    private Book bookForEdit;
    private AbstractViewController mainController;


    @Override
    public void initController(FXMLLoader loader, AbstractViewController parentController, Object... initParameters) {
        initController();

        UUID bookId = (UUID) initParameters[0];
        if (bookId == null) {
            throw new IllegalStateException("Entity key for edit controller is blank!");
        }
        this.bookForEdit = BOOK_REPOSITORY_V2.getById(bookId);
        if (this.bookForEdit == null) {
            throw new IllegalStateException(format("Book by ID \"%s\" not found for edit controller!", bookId));
        }

        this.mainController = parentController;

        nameTextFieldInput.setText(bookForEdit.getName());
        pagesCountFieldInput.setText(String.valueOf(bookForEdit.getPages()));
        coverImagePathFieldInput.setText(bookForEdit.getCoverImgAbsolutePath());

        readCheckBox.setSelected(TRUE.equals(bookForEdit.getRead()));
        autographCheckBox.setSelected(TRUE.equals(bookForEdit.getAutograph()));
        hardCoverCheckBox.setSelected(TRUE.equals(bookForEdit.getHardCover()));

        initComboBoxValue(authorComboBox, bookForEdit.getAuthorId());
        initComboBoxValue(publisherComboBox, bookForEdit.getPublisherId());
        initComboBoxValue(cycleComboBox, bookForEdit.getCycleId());

        priceFieldInput.setText(bookForEdit.getPrice() != null ? String.valueOf(bookForEdit.getPrice()) : "");

        if (bookForEdit.getCycleId() != null) {
            Cycle cycle = CYCLE_REPOSITORY_V2.getById(bookForEdit.getCycleId());
            if (cycle == null) {
                throw new IllegalStateException(format("Not found cycle by id '%s' for book id '%s'", bookForEdit.getCycleId(), bookId));
            }

            cycleEndedCheckBox.setSelected(TRUE.equals(cycle.getEnded()));
            numberInCycleFieldInput.setText(String.valueOf(bookForEdit.getNumberInSeries()));
            totalCountInCycleFieldInput.setText(String.valueOf(cycle.getBooksInCycle()));
        }
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            ButtonAnswer answer = riseAlert(
                CONFIRMATION,
                "Внимание!",
                "Закрыть окно?",
                "Все несохранённые данные будут потеряны"
            );

            if (answer.isOkAnswer()) {
                closeStage();
            }

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            editBook();
        }
    }


    @FXML
    public void cancelEditing() {
        closeStage();
    }

    @FXML
    public void chooseBookCover() {
        selectBookCover();
    }

    @FXML
    public void editBook() {
        saveNewBook(bookForEdit.getAudiobookSites());
    }

    @Override
    protected void doAfterSave(Book savedBook) {
        // Пытаемся обновить изменённую книгу в MainController
        mainController.updateControllerState(savedBook.getId());
    }

    @FXML
    public void addAudioBookSites() {
        loadModalView(
            "Connect audiobook sites",
            "view/audio-book-sites-connections-edit-view.fxml",
            500,
            0,
            bookForEdit.getAudiobookSites()
        );
    }

    private void initComboBoxValue(ComboBox<ComboBoxDTO> comboBox, UUID itemId) {
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            if (Objects.equals(comboBox.getItems().get(i).getId(), itemId)) {
                comboBox.getSelectionModel().select(i);
                return;
            }
        }
    }
}
