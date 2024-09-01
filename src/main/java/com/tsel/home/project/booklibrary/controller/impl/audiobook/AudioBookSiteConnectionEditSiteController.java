package com.tsel.home.project.booklibrary.controller.impl.audiobook;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import com.tsel.home.project.booklibrary.utils.table.ButtonAnswer;
import java.util.Map.Entry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Slf4j
@Getter
public class AudioBookSiteConnectionEditSiteController extends AbstractAudioBookSiteConnectionController {

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TableView<AudioBookSiteDTO> audioBookSiteTable;

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            cancelChanges();
        }
    }

    @FXML
    public void saveConnections() {
        audioBookSiteIds.removeIf(audioBookSite -> !checkedAudioBookSiteItemsMap.get(audioBookSite));
        audioBookSiteIds.addAll(
            checkedAudioBookSiteItemsMap.entrySet()
                .stream()
                .filter(Entry::getValue)
                .map(Entry::getKey)
                .toList()
        );

        closeStage(saveButton);
    }

    @FXML
    public void cancelChanges() {
        ButtonAnswer answer = riseAlert(
            CONFIRMATION,
            "Внимание!",
            "Отменить изменения?",
            "Все изменения не будут сохранены"
        );

        if (answer.isOkAnswer()) {
            closeStage(cancelButton);
        }
    }
}
