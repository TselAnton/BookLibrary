package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.util.Optional.ofNullable;
import static javafx.collections.FXCollections.observableList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.converter.AudioBookSiteConverter;
import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import com.tsel.home.project.booklibrary.repository.impl.AudioBookSiteRepository;
import java.util.Comparator;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioBookSiteController extends AbstractViewController {

    private static final Logger LOGGER = LogManager.getLogger(AudioBookSiteController.class);

    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY = AudioBookSiteRepository.getInstance();
    private static final AudioBookSiteConverter AUDIO_BOOK_SITE_CONVERTER = new AudioBookSiteConverter();

    @FXML
    private TextField addAudioBookSiteTextField;

    @FXML
    private TableView<AudioBookSiteDTO> audioBookSiteTable;

    public AudioBookSiteController() {
        super("Audio Book Sites", "view/audio-book-sites-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        TableView<AudioBookSiteDTO> audioBookSiteTable = (TableView<AudioBookSiteDTO>) loader.getNamespace().get("audioBookSiteTable");

        audioBookSiteTable.setEditable(true);
        audioBookSiteTable.getColumns().add(initAudioBookSiteNumberColumn());
        audioBookSiteTable.getColumns().add(initAudioBookSiteNameColumn());
        audioBookSiteTable.setItems(observableList(
            AUDIO_BOOK_SITE_REPOSITORY.getAll()
                .stream()
                .map(AUDIO_BOOK_SITE_CONVERTER::convert)
                .sorted(Comparator.comparing(AudioBookSiteDTO::getName))
                .toList()
        ));
    }

    private TableColumn<AudioBookSiteDTO, Number> initAudioBookSiteNumberColumn() {
        TableColumn<AudioBookSiteDTO, Number> numberColumn = new TableColumn<>();
        numberColumn.setPrefWidth(30);
        numberColumn.setMinWidth(30);
        numberColumn.setMaxWidth(100);
        numberColumn.setText("№");
        numberColumn.setStyle("-fx-alignment: BASELINE_CENTER");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(audioBookSiteTable.getItems().indexOf(column.getValue()) + 1));
        return numberColumn;
    }

    private TableColumn<AudioBookSiteDTO, String> initAudioBookSiteNameColumn() {
        Callback<TableColumn<AudioBookSiteDTO, String>, TableCell<AudioBookSiteDTO, String>> audioBookSiteNameCellFactory =
            (TableColumn<AudioBookSiteDTO, String> param) -> new AudioBookSiteNameEditableColumn();

        TableColumn<AudioBookSiteDTO, String> audioBookSiteNameColumn = new TableColumn<>();
        audioBookSiteNameColumn.setPrefWidth(262);
        audioBookSiteNameColumn.setMinWidth(262);
        audioBookSiteNameColumn.setMaxWidth(1000);
        audioBookSiteNameColumn.setText("Название");
        audioBookSiteNameColumn.setCellFactory(audioBookSiteNameCellFactory);
        audioBookSiteNameColumn.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getName()));
        return audioBookSiteNameColumn;
    }

    private void updateBookSiteTableItems() {
        audioBookSiteTable.setItems(observableList(
            AUDIO_BOOK_SITE_REPOSITORY.getAll()
                .stream()
                .map(AUDIO_BOOK_SITE_CONVERTER::convert)
                .sorted(Comparator.comparing(AudioBookSiteDTO::getName))
                .toList()
        ));
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case DELETE -> deleteAudioBookSite();
            case ENTER -> addAudioBookSite();
            case ESCAPE -> closeStage();
        }
    }

    private void deleteAudioBookSite() {
        AudioBookSiteDTO clickedEntity = audioBookSiteTable.getSelectionModel().getSelectedItem();
        if (clickedEntity != null) {
            Optional<ButtonType> answer = riseAlert(
                CONFIRMATION,
                "Внимание!",
                "Вы уверены?",
                "Сайт и все его упоминания в книгах будут безвозвратно удалены из библиотеки!"
            );

            if (answer.isPresent() && OK.equals(answer.get().getText())) {
                String deletedEntityKey = AUDIO_BOOK_SITE_CONVERTER.buildEntityKeyByDTO(clickedEntity);
                AudioBookSite deletedEntity = AUDIO_BOOK_SITE_REPOSITORY.getByName(deletedEntityKey);
                AUDIO_BOOK_SITE_REPOSITORY.delete(deletedEntity);
                updateBookSiteTableItems();
            }
        }
    }

    @FXML
    public void addAudioBookSite() {
        String audioBookSiteName = this.addAudioBookSiteTextField.getText();
        if (isBlank(audioBookSiteName)) {
            return;
        }

        AudioBookSite audioBookSite = new AudioBookSite(audioBookSiteName);
        AUDIO_BOOK_SITE_REPOSITORY.save(audioBookSite);
        updateBookSiteTableItems();
        addAudioBookSiteTextField.setText(null);
    }

    private void closeStage() {
        Stage stage = (Stage) addAudioBookSiteTextField.getScene().getWindow();
        stage.close();
    }

    public static class AudioBookSiteNameEditableColumn extends TableCell<AudioBookSiteDTO, String> {

        private TextField inputTextColumn;

        @Override
        public void startEdit() {
            if (!this.isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(inputTextColumn);
                inputTextColumn.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (inputTextColumn != null) {
                        inputTextColumn.setText(getString());
                    }
                    setText(null);
                    setGraphic(inputTextColumn);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            inputTextColumn = new TextField(getString());
            inputTextColumn.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            inputTextColumn.setOnAction((e) -> commitEdit(inputTextColumn.getText()));
            inputTextColumn.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    LOGGER.info("Committing editable text: {}", inputTextColumn.getText());
                    commitEdit(inputTextColumn.getText());
                }
            });
        }

        private String getString() {
            return ofNullable(this.getItem()).orElse("");
        }
    }
}
