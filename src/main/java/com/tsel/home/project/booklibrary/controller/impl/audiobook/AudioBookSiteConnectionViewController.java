package com.tsel.home.project.booklibrary.controller.impl.audiobook;

import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;

@Getter
public class AudioBookSiteConnectionViewController extends AbstractAudioBookSiteConnectionController {

    @FXML
    private TableView<AudioBookSiteDTO> audioBookSiteTable;

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            closeStage(audioBookSiteTable);
        }
    }
}
