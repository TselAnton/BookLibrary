package com.tsel.home.project.booklibrary.controller.impl.audiobook;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import com.tsel.home.project.booklibrary.utils.table.CustomCheckBoxTableCell;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAudioBookSiteConnectionController extends AbstractAudioBookSiteController {

    protected List<UUID> audioBookSiteIds;
    protected final Map<UUID, Boolean> checkedAudioBookSiteItemsMap = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void initController(FXMLLoader loader, AbstractViewController parentController, Object... initParameters) {
        audioBookSiteIds = (List<UUID>) initParameters[0];
        if (audioBookSiteIds == null) {
            throw new IllegalStateException("AudioBook sites collections for connection controller is null!");
        }

        // Заполнение мапы для отслеживания чекбоксов, где ключ - ID сайта, значение - выбран ли сайт для текущей книги
        AUDIO_BOOK_SITE_REPOSITORY_V2.getAll()
            .stream()
            .map(AudioBookSite::getId)
            .forEach(audioBookSiteId -> checkedAudioBookSiteItemsMap.put(audioBookSiteId, audioBookSiteIds.contains(audioBookSiteId)));

        TableView<AudioBookSiteDTO> audioBookSiteTable = (TableView<AudioBookSiteDTO>) loader.getNamespace().get("audioBookSiteTable");
        initTableView(audioBookSiteTable,
            initAudioBookSiteNumberColumn(),
            initAudioBookSiteCheckBoxColumn(),
            initAudioBookSiteNameColumn()
        );
    }

    protected abstract TableView<AudioBookSiteDTO> getAudioBookSiteTable();

    private TableColumn<AudioBookSiteDTO, Number> initAudioBookSiteNumberColumn() {
        TableColumn<AudioBookSiteDTO, Number> numberColumn = new TableColumn<>();
        numberColumn.setPrefWidth(10);
        numberColumn.setMinWidth(50);
        numberColumn.setMaxWidth(100);
        numberColumn.setStyle("-fx-alignment: BASELINE_CENTER");
        numberColumn.setText("№");
        numberColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(getAudioBookSiteTable().getItems().indexOf(column.getValue()) + 1));
        return numberColumn;
    }

    private TableColumn<AudioBookSiteDTO, CheckBox> initAudioBookSiteCheckBoxColumn() {
        TableColumn<AudioBookSiteDTO, CheckBox> connectionColumn = new TableColumn<>();
        connectionColumn.setPrefWidth(70);
        connectionColumn.setMinWidth(70);
        connectionColumn.setMaxWidth(200);
        connectionColumn.setText("Наличие");

        Callback<TableColumn<AudioBookSiteDTO, CheckBox>, TableCell<AudioBookSiteDTO, CheckBox>> audioBookSiteConnectionCellFactory =
            (TableColumn<AudioBookSiteDTO, CheckBox> param) -> new CustomCheckBoxTableCell<>() {
                @Override
                protected boolean setSelectedOnInit(UUID checkBoxId) {
                    return checkedAudioBookSiteItemsMap.get(checkBoxId);
                }

                @Override
                protected void handleActionEvent(ActionEvent event) {
                    log.debug("Handle event for checkBox '{}', status: {}", this.getCheckBoxId(), this.isCheckBoxSelected());
                    checkedAudioBookSiteItemsMap.put(this.getCheckBoxId(), this.isCheckBoxSelected());
                }
            };

        connectionColumn.setCellFactory(audioBookSiteConnectionCellFactory);
        connectionColumn.setCellValueFactory(column -> {
            CheckBox checkBox = new CheckBox();
            checkBox.setId(column.getValue().getId().toString());
            checkBox.setSelected(checkedAudioBookSiteItemsMap.get(column.getValue().getId()));
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
}
