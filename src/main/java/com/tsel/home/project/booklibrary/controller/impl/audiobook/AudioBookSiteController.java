package com.tsel.home.project.booklibrary.controller.impl.audiobook;

import static com.tsel.home.project.booklibrary.utils.CollectionUtils.isNotEmpty;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static javafx.collections.FXCollections.observableList;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import com.tsel.home.project.booklibrary.utils.elements.ButtonAnswer;
import com.tsel.home.project.booklibrary.utils.table.AudioBookSiteNameEditableColumn;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class AudioBookSiteController extends AbstractAudioBookSiteController {

    @FXML
    private TextField addAudioBookSiteTextField;

    @FXML
    private TableView<AudioBookSiteDTO> audioBookSiteTable;

    @Override
    @SuppressWarnings("unchecked")
    public void initController(FXMLLoader loader, AbstractViewController parentController, Object... initParameters) {
        TableView<AudioBookSiteDTO> audioBookSiteTableView = (TableView<AudioBookSiteDTO>) loader.getNamespace().get("audioBookSiteTable");
        initTableView(audioBookSiteTableView, initAudioBookSiteNumberColumn(), initAudioBookSiteNameColumn());
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case DELETE -> deleteAudioBookSite();
            case ENTER -> addAudioBookSite();
            case ESCAPE -> closeStage(addAudioBookSiteTextField);
            default -> {
                // DO NOTHING
            }
        }
    }

    @FXML
    public void addAudioBookSite() {
        String audioBookSiteName = this.addAudioBookSiteTextField.getText();
        if (isBlank(audioBookSiteName)) {
            return;
        }

        AudioBookSite audioBookSite = new AudioBookSite(null, audioBookSiteName);
        AUDIO_BOOK_SITE_REPOSITORY_V2.save(audioBookSite);

        updateBookSiteTableItems(audioBookSiteTable);
        addAudioBookSiteTextField.setText(null);
    }

    private void deleteAudioBookSite() {
        AudioBookSiteDTO clickedEntity = audioBookSiteTable.getSelectionModel().getSelectedItem();
        if (clickedEntity != null) {
            ButtonAnswer answer = riseAlert(
                CONFIRMATION,
                "Внимание!",
                "Вы уверены?",
                "Сайт и все его упоминания в книгах будут безвозвратно удалены из библиотеки!"
            );

            if (answer.isOkAnswer()) {
                BOOK_REPOSITORY_V2.getAll()
                    .stream()
                    .filter(book -> isNotEmpty(book.getAudioBookSiteIds()))
                    .forEach(book -> {
                        book.getAudioBookSiteIds().remove(clickedEntity.getId());
                        BOOK_REPOSITORY_V2.save(book);
                    });

                AUDIO_BOOK_SITE_REPOSITORY_V2.deleteById(clickedEntity.getId());
                updateBookSiteTableItems(audioBookSiteTable);
            }
        }
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
}