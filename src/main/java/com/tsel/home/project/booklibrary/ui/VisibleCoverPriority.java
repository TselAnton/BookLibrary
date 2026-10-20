package com.tsel.home.project.booklibrary.ui;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Передаёт загрузчику фактически видимые строки после раскладки VirtualFlow.
 * Учитывает прокрутку, замену данных, сортировку и размер окна без опроса файлов.
 */
public final class VisibleCoverPriority implements AutoCloseable {

    private final TableView<BookDTO> table;
    private final Consumer<Set<UUID>> prioritize;
    private final Runnable afterLayout = this::update;
    private final ChangeListener<Scene> sceneListener = (observable, previous, scene) -> attach(previous, scene);
    private Set<UUID> visibleIds = Set.of();

    public VisibleCoverPriority(TableView<BookDTO> table, Consumer<Set<UUID>> prioritize) {
        this.table = table;
        this.prioritize = prioritize;
        table.sceneProperty().addListener(sceneListener);
        attach(null, table.getScene());
    }

    @Override
    public void close() {
        table.sceneProperty().removeListener(sceneListener);
        attach(table.getScene(), null);
    }

    private void attach(Scene previous, Scene scene) {
        if (previous != null) {
            previous.removePostLayoutPulseListener(afterLayout);
        }
        publish(Set.of());
        if (scene != null) {
            scene.addPostLayoutPulseListener(afterLayout);
        }
    }

    private void update() {
        Scene scene = table.getScene();
        if (!table.isVisible() || scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()
            || !(table.lookup(".virtual-flow") instanceof VirtualFlow<?> flow)) {
            publish(Set.of());
            return;
        }
        IndexedCell<?> first = flow.getFirstVisibleCell();
        IndexedCell<?> last = flow.getLastVisibleCell();
        Set<UUID> ids = new HashSet<>();
        if (first != null && last != null && table.getItems() != null) {
            int end = Math.min(last.getIndex(), table.getItems().size() - 1);
            for (int index = Math.max(0, first.getIndex()); index <= end; index++) {
                ids.add(table.getItems().get(index).getId());
            }
        }
        publish(ids);
    }

    private void publish(Set<UUID> ids) {
        if (!visibleIds.equals(ids)) {
            visibleIds = Set.copyOf(ids);
            prioritize.accept(visibleIds);
        }
    }
}
