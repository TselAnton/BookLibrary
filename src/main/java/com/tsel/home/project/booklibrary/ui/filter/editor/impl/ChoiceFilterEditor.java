package com.tsel.home.project.booklibrary.ui.filter.editor.impl;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.condition.TextCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.ui.LineIcon;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Редактирует выбор вариантов независимыми чекбоксами.
 * По умолчанию создаёт первые пять строк; поиск показывает все совпадения, сохраняя выбор.
 * Неотображаемые варианты не требуют создания контролов до раскрытия или поиска.
 */
public class ChoiceFilterEditor implements FilterEditor {

    private static final int COLLAPSED_OPTION_COUNT = 5;

    private final VBox content = new VBox(4);
    private final VBox rows = new VBox();
    private final TextField search = new TextField();
    private final Button expand = new Button();
    private final LineIcon chevron = new LineIcon("down");
    private final Map<Object, CheckBox> choices = new LinkedHashMap<>();
    private final Set<Object> selected = new LinkedHashSet<>();
    private final FilterField field;
    private final Runnable changed;
    private List<FilterOption> options = List.of();
    private boolean expanded;

    /**
     * Создаёт редактор и восстанавливает исходное условие.
     * @param field метаданные и процессор поля
     * @param condition исходное условие либо null
     * @param changed уведомление о правках черновика
     */
    public ChoiceFilterEditor(FilterField field, FilterCondition condition, Runnable changed) {
        this.field = field;
        this.changed = changed;
        if (condition instanceof ChoiceCondition choice) {
            selected.addAll(choice.values());
        }
        search.setPromptText("Найти в списке…");
        search.setAccessibleText("Найти: " + field.label());
        search.setId("filter-" + field.id() + "-search");
        search.textProperty().addListener((observable, oldValue, newValue) -> filterRows());
        search.setVisible(false);
        search.setManaged(false);
        expand.setId("filter-" + field.id() + "-expand");
        expand.getStyleClass().addAll("text-button", "filter-choice-toggle");
        expand.setGraphic(chevron);
        expand.setOnAction(event -> {
            expanded = !expanded;
            filterRows();
        });
        expand.setVisible(false);
        expand.setManaged(false);
        content.getChildren().addAll(search, rows, expand);
    }

    /**
     * Возвращает переиспользуемый корневой узел редактора.
     * @return узел, размещаемый внутри группы фильтра
     */
    @Override
    public Node node() {
        return content;
    }

    /**
     * Возвращает типизированное условие текущего ввода; пустой ввод не ограничивает выборку.
     * @return условие либо null
     * @throws IllegalArgumentException при некорректном вводе
     */
    @Override
    public FilterCondition condition() {
        return selected.isEmpty() ? null : new ChoiceCondition(selected);
    }

    /**
     * Сообщает, нужны ли редактору варианты и их фоновые счётчики.
     * @return true для редакторов справочных списков
     */
    @Override
    public boolean requiresOptions() {
        return true;
    }

    /**
     * Восстанавливает применённое условие без создания нового редактора.
     * @param condition сохранённое условие или null для сброса
     * @return true при поддержке восстановления; иначе панель создаст редактор заново
     */
    @Override
    public boolean restore(FilterCondition condition) {
        selected.clear();
        if (condition instanceof ChoiceCondition choice) {
            selected.addAll(choice.values());
        }
        expanded = false;
        search.clear();
        filterRows();
        return true;
    }

    /**
     * Обновляет варианты и счётчики, сохраняя текущие выбранные значения.
     * @param options актуальные канонические варианты с подписями и количеством книг
     */
    @Override
    public void refresh(List<FilterOption> options) {
        this.options = List.copyOf(options);
        Set<Object> available = new LinkedHashSet<>();
        options.forEach(option -> available.add(option.value()));
        choices.keySet().removeIf(value -> !available.contains(value));
        filterRows();
    }

    private CheckBox createChoice(Object value) {
        CheckBox checkBox = new CheckBox();
        checkBox.setMaxWidth(Double.MAX_VALUE);
        checkBox.setWrapText(true);
        checkBox.setSelected(selected.contains(value));
        checkBox.setContentDisplay(ContentDisplay.RIGHT);
        checkBox.setGraphicTextGap(4);
        Label count = new Label();
        count.getStyleClass().add("filter-option-count");
        checkBox.setGraphic(count);
        checkBox.getStyleClass().add("filter-option");
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                selected.add(value);
            } else {
                selected.remove(value);
            }
            changed.run();
        });
        return checkBox;
    }

    private void filterRows() {
        String query = TextCondition.normalize(search.getText().trim());
        boolean searching = !query.isEmpty();
        search.setVisible(options.size() > COLLAPSED_OPTION_COUNT || searching);
        search.setManaged(search.isVisible());
        List<Node> visibleRows = new ArrayList<>();
        for (int index = 0; index < options.size(); index++) {
            FilterOption option = options.get(index);
            boolean visible = searching ? TextCondition.normalize(option.label()).contains(query)
                : expanded || index < COLLAPSED_OPTION_COUNT;
            if (!visible) {
                continue;
            }
            CheckBox choice = choices.computeIfAbsent(option.value(), this::createChoice);
            choice.setId("filter-" + field.id() + "-option-" + index);
            if (!option.label().equals(choice.getText())) {
                choice.setText(option.label());
            }
            choice.setSelected(selected.contains(option.value()));
            ((Label) choice.getGraphic()).setText("(" + option.count() + ")");
            choice.setAccessibleText(field.label() + ": " + option.label() + ", книг: " + option.count());
            visibleRows.add(choice);
        }
        if (!rows.getChildren().equals(visibleRows)) {
            rows.getChildren().setAll(visibleRows);
        }
        boolean overflow = !searching && options.size() > COLLAPSED_OPTION_COUNT;
        expand.setVisible(overflow);
        expand.setManaged(overflow);
        int hidden = Math.max(0, options.size() - COLLAPSED_OPTION_COUNT);
        expand.setText(expanded ? "Свернуть" : "Показать ещё " + hidden);
        expand.setAccessibleText(field.label() + (expanded ? ": свернуть список" : ": показать все значения, скрыто " + hidden));
        chevron.setRotate(expanded ? 180 : 0);
    }
}
