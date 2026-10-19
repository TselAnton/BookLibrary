package com.tsel.home.project.booklibrary.javafx.filter.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.ui.filter.editor.impl.ChoiceFilterEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

class ChoiceFilterEditorTest extends UnitJavaFXTest {

    @Test
    void testLargeListsOnlyCreateVisibleControlsAndReuseThem() throws Exception {
        onFxThread(() -> {
            ChoiceFilterEditor editor = editor();
            List<FilterOption> options = options(10000);
            editor.refresh(options);
            assertEquals(5, editor.node().lookupAll(".filter-option").size());
            Node first = editor.node().lookup("#filter-author-option-0");
            editor.restore(null);
            editor.refresh(options);
            assertSame(first, editor.node().lookup("#filter-author-option-0"));
            assertNull(editor.condition());
            search(editor).setText("Автор 9999");
            assertEquals(List.of("Автор 9999"), visible(editor));
            assertEquals(1, editor.node().lookupAll(".filter-option").size());
        });
    }

    @Test
    void testListsDefaultToFiveValuesAndCanExpandWithoutChangingSelection() throws Exception {
        onFxThread(() -> {
            ChoiceFilterEditor editor = editor();
            editor.refresh(options(8));
            assertEquals(List.of("Автор 1", "Автор 2", "Автор 3", "Автор 4", "Автор 5"), visible(editor));
            assertTrue(search(editor).isVisible());
            toggle(editor).fire();
            assertEquals(8, visible(editor).size());
            toggle(editor).fire();
            assertEquals(5, visible(editor).size());
            assertEquals(new ChoiceCondition(Set.of("Автор 7")), editor.condition());
        });
    }

    @Test
    void testSearchingRevealsAllMatchesAndClearingRestoresManualExpansion() throws Exception {
        onFxThread(() -> {
            ChoiceFilterEditor editor = editor();
            editor.refresh(options(8));
            search(editor).setText("Автор");
            assertEquals(8, visible(editor).size());
            assertFalse(toggle(editor).isVisible());
            search(editor).setText("Автор 8");
            assertEquals(List.of("Автор 8"), visible(editor));
            search(editor).clear();
            assertEquals(5, visible(editor).size());
            toggle(editor).fire();
            search(editor).setText("Нет совпадений");
            assertTrue(visible(editor).isEmpty());
            search(editor).clear();
            assertEquals(8, visible(editor).size());
        });
    }

    @Test
    void testSearchingOptionsIgnoresCaseAndRussianYoWithoutMergingChoices() throws Exception {
        onFxThread(() -> {
            ChoiceFilterEditor editor = editor();
            List<FilterOption> options = new ArrayList<>(options(5));
            options.add(new FilterOption("Семён", "Семён", 2));
            options.add(new FilterOption("Семен", "Семен", 1));
            editor.refresh(options);
            editor.restore(new ChoiceCondition(Set.of("Семён")));
            search(editor).setText("СЕМЕН");
            assertEquals(List.of("Семён", "Семен"), visible(editor));
            assertEquals(new ChoiceCondition(Set.of("Семён")), editor.condition());
            search(editor).setText("СЕМЁН");
            assertEquals(List.of("Семён", "Семен"), visible(editor));
        });
    }

    @Test
    void testSixValuesHaveSearchAndRefreshPreservesSelectionAndExpansion() throws Exception {
        onFxThread(() -> {
            ChoiceFilterEditor editor = editor();
            editor.refresh(options(6));
            assertTrue(search(editor).isVisible());
            assertTrue(toggle(editor).isVisible());
            assertEquals(5, visible(editor).size());
            editor.refresh(options(8));
            toggle(editor).fire();
            List<FilterOption> reversed = new ArrayList<>(options(8));
            Collections.reverse(reversed);
            editor.refresh(reversed);
            assertEquals(8, visible(editor).size());
            toggle(editor).fire();
            assertEquals(List.of("Автор 8", "Автор 7", "Автор 6", "Автор 5", "Автор 4"), visible(editor));
            assertEquals(new ChoiceCondition(Set.of("Автор 7")), editor.condition());
            search(editor).setText("Автор 8");
            editor.refresh(options(5));
            assertTrue(search(editor).isVisible(), "An active query must remain editable when the available list shrinks");
            assertTrue(visible(editor).isEmpty());
            search(editor).clear();
            assertFalse(search(editor).isVisible());
            assertFalse(toggle(editor).isVisible());
            assertEquals(5, visible(editor).size());
        });
    }

    private ChoiceFilterEditor editor() {
        return new ChoiceFilterEditor(new FilterCatalog(TestRow.class).fields().get(0), new ChoiceCondition(Set.of("Автор 7")), () -> {});
    }

    private List<FilterOption> options(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(index -> new FilterOption("Автор " + index, "Автор " + index, index))
            .toList();
    }

    private List<String> visible(ChoiceFilterEditor editor) {
        Node choice = editor.node().lookup(".filter-option");
        if (choice == null) {
            return List.of();
        }
        return choice.getParent().getChildrenUnmodifiable().stream()
            .map(CheckBox.class::cast)
            .filter(CheckBox::isManaged)
            .map(CheckBox::getText)
            .toList();
    }

    private TextField search(ChoiceFilterEditor editor) {
        return (TextField) editor.node().lookup("#filter-author-search");
    }

    private Button toggle(ChoiceFilterEditor editor) {
        return (Button) editor.node().lookup("#filter-author-expand");
    }

    private void onFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static class TestRow {

        @FilterProperty(label = "Автор", editor = "choices", allowEmpty = false)
        private String author;
    }
}
