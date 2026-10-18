package com.tsel.home.project.booklibrary.controller.impl.book;


import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class NewBookController extends AbstractBookController {


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
    private TextField publicationYearFieldInput;

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

    @FXML
    private ComboBox<ComboBoxDTO> genreComboBox;

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
        cancelForm();
    }

    @FXML
    public void chooseBookCover() {
        selectBookCover();
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) { handleFormKey(keyEvent); }


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
