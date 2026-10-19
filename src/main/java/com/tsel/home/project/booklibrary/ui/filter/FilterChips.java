package com.tsel.home.project.booklibrary.ui.filter;

import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.ui.LineIcon;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import lombok.Getter;

/**
 * Показывает применённые условия компактными тегами с закрытием и раскрытием дополнительных строк.
 * Состояние раскрытия принадлежит компоненту и сохраняется при обновлении данных и окон книги.
 */
public class FilterChips extends HBox {

    private final TagPane tags = new TagPane();
    private final Button expand = new Button();
    private final LineIcon chevron = new LineIcon("down");
    private final Tooltip expandTooltip = new Tooltip();
    private final Label hiddenCount = new Label();
    private final HBox expansion = new HBox(4, expand, hiddenCount);
    private final Button clear = new Button("Сбросить");

    @Getter
    private boolean expanded;

    /**
     * Создаёт полосу тегов с отдельными областями раскрытия и сброса.
     * @param reset действие полного сброса условий
     */
    public FilterChips(Runnable reset) {
        setId("activeFilters");
        setSpacing(8);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add("active-filter-strip");
        HBox.setHgrow(tags, Priority.ALWAYS);
        tags.setMinWidth(0);
        expand.setId("expandFiltersButton");
        expand.getStyleClass().addAll("text-button", "filter-tags-toggle");
        expand.setGraphic(chevron);
        expand.setTooltip(expandTooltip);
        hiddenCount.setId("hiddenFiltersCount");
        hiddenCount.getStyleClass().add("filter-hidden-count");
        hiddenCount.setMouseTransparent(true);
        expansion.setAlignment(Pos.CENTER_LEFT);
        expansion.setMaxHeight(USE_PREF_SIZE);
        expansion.setMinWidth(USE_PREF_SIZE);
        expand.setOnAction(event -> {
            expanded = !expanded;
            tags.requestLayout();
            requestLayout();
        });
        clear.setId("resetFiltersButton");
        clear.setMinWidth(USE_PREF_SIZE);
        clear.getStyleClass().addAll("text-button", "filter-tag-action");
        clear.setOnAction(event -> reset.run());
        getChildren().addAll(tags, expansion, clear);
        setVisible(false);
        setManaged(false);
    }

    @Override
    protected void layoutChildren() {
        double available = Math.max(1, getWidth() - getInsets().getLeft() - getInsets().getRight()
            - clear.prefWidth(-1) - getSpacing());
        boolean overflow = tags.overflows(available);
        expand.setVisible(overflow);
        expand.setManaged(overflow);
        expansion.setVisible(overflow);
        expansion.setManaged(overflow);
        hiddenCount.setVisible(overflow && !expanded);
        hiddenCount.setManaged(hiddenCount.isVisible());
        super.layoutChildren();
    }

    /**
     * Перестраивает применённые теги, сохраняя выбранное состояние раскрытия строк.
     * @param catalog каталог названий и описаний условий
     * @param conditions применённые условия
     * @param remove действие удаления одного условия по его идентификатору
     */
    public void update(FilterCatalog catalog, Map<String, FilterCondition> conditions, Consumer<String> remove) {
        tags.chips.clear();
        tags.getChildren().clear();
        catalog.fields().forEach(field -> {
            FilterCondition condition = conditions.get(field.id());
            if (condition == null) {
                return;
            }
            String text = field.label() + ": " + condition.describe(field);
            Label label = new Label(text);
            label.setWrapText(true);
            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            Button close = SageTheme.iconButton("close", "Снять фильтр: " + text, () -> remove.accept(field.id()));
            close.getStyleClass().add("chip-close");
            HBox chip = new HBox(4, label, close);
            chip.setId("chip-" + field.id());
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.getStyleClass().add("filter-chip");
            tags.chips.add(chip);
            tags.getChildren().add(chip);
        });
        setVisible(!tags.chips.isEmpty());
        setManaged(isVisible());
        tags.requestLayout();
        requestLayout();
    }

    private class TagPane extends Pane {

        private final List<HBox> chips = new ArrayList<>();

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computePrefHeight(double width) {
            return arrange(Math.max(1, width), false);
        }

        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computePrefWidth(double height) {
            return 0;
        }

        @Override
        protected void layoutChildren() {
            arrange(Math.max(1, getWidth()), true);
        }

        private double arrange(double width, boolean layout) {
            double x = 0;
            double y = 0;
            double rowHeight = 0;
            int hidden = 0;
            for (HBox chip : chips) {
                double chipWidth = Math.min(width, chip.prefWidth(-1));
                double chipHeight = Math.max(26, chip.prefHeight(chipWidth));
                if (x > 0 && x + chipWidth > width) {
                    x = 0;
                    y += rowHeight + 6;
                    rowHeight = 0;
                }
                boolean visible = expanded || y == 0;
                if (y > 0) {
                    hidden++;
                }
                if (layout) {
                    chip.setVisible(visible);
                    chip.setManaged(visible);
                    if (visible) {
                        chip.resizeRelocate(x, y, chipWidth, chipHeight);
                    }
                }
                if (visible) {
                    rowHeight = Math.max(rowHeight, chipHeight);
                }
                x += chipWidth + 6;
            }
            if (layout) {
                chevron.setRotate(expanded ? 180 : 0);
                hiddenCount.setText(Integer.toString(hidden));
                hiddenCount.setAccessibleText("Скрытых фильтров: " + hidden);
                String action = expanded ? "Свернуть теги фильтров" : "Показать скрытые теги: " + hidden;
                expand.setAccessibleText(action);
                expandTooltip.setText(action);
            }
            return chips.isEmpty() ? 0 : expanded ? y + rowHeight : firstRowHeight(width);
        }

        private boolean overflows(double width) {
            double x = 0;
            for (HBox chip : chips) {
                double chipWidth = Math.min(width, chip.prefWidth(-1));
                if (x > 0 && x + chipWidth > width) {
                    return true;
                }
                x += chipWidth + 6;
            }
            return false;
        }

        private double firstRowHeight(double width) {
            double x = 0;
            double height = 26;
            for (Node chip : chips) {
                double chipWidth = Math.min(width, chip.prefWidth(-1));
                if (x > 0 && x + chipWidth > width) {
                    break;
                }
                height = Math.max(height, chip.prefHeight(chipWidth));
                x += chipWidth + 6;
            }
            return height;
        }
    }
}
