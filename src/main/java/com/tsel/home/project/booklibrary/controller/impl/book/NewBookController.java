package com.tsel.home.project.booklibrary.controller.impl.book;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import com.tsel.home.project.booklibrary.utils.elements.ButtonAnswer;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
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
public class NewBookController extends AbstractBookController {

    private static final Logger log = LogManager.getLogger(NewBookController.class);

    private static final Gson GSON = MyGson.buildGson();

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

    private final List<UUID> audioBookSiteIds = new ArrayList<>();


    @FXML
    public void initialize() {
        initController();
    }

    @FXML
    public void cancelAdding() {
        closeStage(getExitButton());
    }

    @FXML
    public void chooseBookCover() {
        selectBookCover();
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
                closeStage(getExitButton());
            }

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            addBook();
        }
    }

    @FXML
    public void addBook() {
        saveNewBook(null, audioBookSiteIds);
    }

    @FXML
    public void addAudioBookSites() {
        loadModalView(
            "Connect audiobook sites",
            "view/audio-book-sites-connections-edit-view.fxml",
            500,
            0,
            audioBookSiteIds
        );
    }
}
