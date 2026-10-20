package com.tsel.home.project.booklibrary.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.ui.VisibleCoverPriority;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class VisibleCoverPriorityTest extends UnitJavaFXTest {

    @Test
    void shouldTrackVisibleRowsAfterScrollSortFilterResizeAndDetach() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            List<BookDTO> books = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                books.add(BookDTO.builder().id(UUID.randomUUID()).name("Book %03d".formatted(i)).build());
            }
            TableView<BookDTO> table = new TableView<>(FXCollections.observableArrayList(books));
            table.setFixedCellSize(32);
            TableColumn<BookDTO, String> name = new TableColumn<>("Name");
            name.setCellValueFactory(value -> new ReadOnlyStringWrapper(value.getValue().getName()));
            table.getColumns().add(name);
            List<Set<UUID>> snapshots = new ArrayList<>();
            Stage stage = new Stage();
            StackPane root = new StackPane(table);
            Scene scene = new Scene(root, 400, 240);
            stage.setScene(scene);
            try (VisibleCoverPriority priority = new VisibleCoverPriority(table, snapshots::add)) {
                stage.show();
                pulse(scene);
                Set<UUID> initial = latest(snapshots);
                assertTrue(initial.contains(books.get(0).getId()));
                assertTrue(initial.size() < 10, "Offscreen and measurement cells must not be promoted");
                assertFalse(initial.contains(books.get(50).getId()));

                table.scrollTo(50);
                pulse(scene);
                assertTrue(latest(snapshots).contains(books.get(50).getId()));
                assertFalse(latest(snapshots).contains(books.get(0).getId()));

                name.setSortType(TableColumn.SortType.DESCENDING);
                table.getSortOrder().add(name);
                table.sort();
                table.scrollTo(0);
                pulse(scene);
                assertTrue(latest(snapshots).contains(books.get(99).getId()));
                assertFalse(latest(snapshots).contains(books.get(0).getId()));

                int beforeResize = latest(snapshots).size();
                stage.setHeight(450);
                pulse(scene);
                assertTrue(latest(snapshots).size() > beforeResize);

                table.setItems(FXCollections.observableArrayList(books.get(70), books.get(80)));
                pulse(scene);
                assertEquals(Set.of(books.get(70).getId(), books.get(80).getId()), latest(snapshots));
                table.getItems().clear();
                pulse(scene);
                assertEquals(Set.of(), latest(snapshots));

                table.setItems(FXCollections.observableArrayList(books));
                pulse(scene);
                assertFalse(latest(snapshots).isEmpty());
                root.getChildren().clear();
                assertEquals(Set.of(), latest(snapshots));
                root.getChildren().add(table);
                pulse(scene);
                assertFalse(latest(snapshots).isEmpty());
            } finally {
                stage.hide();
            }
            assertEquals(Set.of(), latest(snapshots));
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private static Set<UUID> latest(List<Set<UUID>> snapshots) {
        assertFalse(snapshots.isEmpty());
        return snapshots.get(snapshots.size() - 1);
    }

    private static void pulse(Scene scene) {
        Object key = new Object();
        scene.addPostLayoutPulseListener(new Runnable() {
            @Override
            public void run() {
                scene.removePostLayoutPulseListener(this);
                Platform.runLater(() -> Platform.exitNestedEventLoop(key, null));
            }
        });
        Platform.requestNextPulse();
        Platform.enterNestedEventLoop(key);
    }
}
