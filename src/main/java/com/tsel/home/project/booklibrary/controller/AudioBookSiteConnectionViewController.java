package com.tsel.home.project.booklibrary.controller;

import static javafx.collections.FXCollections.observableList;

import com.tsel.home.project.booklibrary.converter.AudioBookSiteConverter;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.utils.CustomCheckBoxTableCell;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AudioBookSiteConnectionViewController extends AbstractViewController {

    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY = AudioBookSiteRepository.getInstance();
    private static final AudioBookSiteConverter AUDIO_BOOK_SITE_CONVERTER = new AudioBookSiteConverter();

    @FXML
    private TableView<AudioBookSiteDTO> audioBookSiteTable;

    private List<String> audioBookSites;
    private final Map<String, Boolean> checkedAudioBookItemsMap = new HashMap<>();

    public AudioBookSiteConnectionViewController() {
        super("Audio Book Sites", "view/audio-book-sites-connections-view.fxml");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initController(AbstractViewController parentController, Object... initParameters) {
        audioBookSites = (List<String>) initParameters[0];
        if (audioBookSites == null) {
            throw new IllegalStateException("AudioBook sites collections for connection controller is null!");
        }

        AUDIO_BOOK_SITE_REPOSITORY.getAll()
            .stream()
            .map(AudioBookSite::getKey)
            .forEach(audioBookName -> checkedAudioBookItemsMap.put(audioBookName, audioBookSites.contains(audioBookName)));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterInitScene(FXMLLoader loader) {
        TableView<AudioBookSiteDTO> audioBookSiteTable = (TableView<AudioBookSiteDTO>) loader.getNamespace().get("audioBookSiteTable");

        audioBookSiteTable.setEditable(false);
        audioBookSiteTable.getColumns().add(initAudioBookSiteNumberColumn());
        audioBookSiteTable.getColumns().add(initAudioBookSiteCheckBoxColumn());
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
        numberColumn.setPrefWidth(10);
        numberColumn.setMinWidth(50);
        numberColumn.setMaxWidth(100);
        numberColumn.setStyle("-fx-alignment: BASELINE_CENTER");
        numberColumn.setText("№");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(audioBookSiteTable.getItems().indexOf(column.getValue()) + 1));
        return numberColumn;
    }

    private TableColumn<AudioBookSiteDTO, CheckBox> initAudioBookSiteCheckBoxColumn() {
        TableColumn<AudioBookSiteDTO, CheckBox> connectionColumn = new TableColumn<>();
        connectionColumn.setPrefWidth(70);
        connectionColumn.setMinWidth(70);
        connectionColumn.setMaxWidth(200);
        connectionColumn.setText("Наличие");

        Callback<TableColumn<AudioBookSiteDTO, CheckBox>, TableCell<AudioBookSiteDTO, CheckBox>> audioBookSiteConnectionCellFactory =
            (TableColumn<AudioBookSiteDTO, CheckBox> param) -> new CustomCheckBoxTableCell<> () {
            @Override
            protected boolean setSelectedOnInit(String checkBoxId) {
                return checkedAudioBookItemsMap.get(checkBoxId);
            }
        };

        connectionColumn.setCellFactory(audioBookSiteConnectionCellFactory);
        connectionColumn.setCellValueFactory(column -> {
            CheckBox checkBox = new CheckBox();
            checkBox.setId(column.getValue().getName());
            checkBox.setSelected(checkedAudioBookItemsMap.get(column.getValue().getName()));
            return new SimpleObjectProperty<>(checkBox);
        });
        return connectionColumn;
    }

    private TableColumn<AudioBookSiteDTO, String> initAudioBookSiteNameColumn() {
        TableColumn<AudioBookSiteDTO, String> audioBookSiteNameColumn = new TableColumn<>();
        audioBookSiteNameColumn.setPrefWidth(324);
        audioBookSiteNameColumn.setMinWidth(324);
        audioBookSiteNameColumn.setMaxWidth(1000);
        audioBookSiteNameColumn.setText("Название");
        audioBookSiteNameColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(column.getValue().getName()));
        return audioBookSiteNameColumn;
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            Stage stage = (Stage) audioBookSiteTable.getScene().getWindow();
            stage.close();
        }
    }
}
