package com.tsel.home.project.booklibrary.controller.impl.audiobook;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static javafx.collections.FXCollections.observableList;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.converter.AudioBookSiteConverter;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepositoryV2;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;
import java.util.Comparator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public abstract class AbstractAudioBookSiteController extends AbstractViewController {

    protected final AudioBookSiteConverter audioBookSiteConverter = new AudioBookSiteConverter();
    protected final AudioBookSiteRepositoryV2 audioBookSiteRepository = getBean(AudioBookSiteRepositoryV2.class);

    @Override
    protected AnchorPane getMainAnchorPane() {
        return null;
    }

    /**
     * Заполнить таблицу сайтами аудиокниг
     * @param audioBookSiteTable таблица для заполнения
     */
    protected void updateBookSiteTableItems(TableView<AudioBookSiteDTO> audioBookSiteTable) {
        audioBookSiteTable.setItems(observableList(
            audioBookSiteRepository.getAll()
                .stream()
                .map(audioBookSiteConverter::convert)
                .sorted(Comparator.comparing(AudioBookSiteDTO::getName))
                .toList()
        ));
    }

    /**
     * Инициализация начальной таблицы отображения
     * @param audioBookSiteTableView таблица для отображения
     */
    @SafeVarargs
    protected final void initTableView(TableView<AudioBookSiteDTO> audioBookSiteTableView, TableColumn<AudioBookSiteDTO, ?>... tableColumns) {
        audioBookSiteTableView.setEditable(true);
        audioBookSiteTableView.getColumns().addAll(tableColumns);
        updateBookSiteTableItems(audioBookSiteTableView);
    }
}
