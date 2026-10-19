package com.tsel.home.project.booklibrary.ui.filter;

import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.cache.FilterOptionCache;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.FilterOption;
import com.tsel.home.project.booklibrary.filter.model.FilterState;
import com.tsel.home.project.booklibrary.ui.LineIcon;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditor;
import com.tsel.home.project.booklibrary.ui.filter.editor.FilterEditorRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Показывает выезжающую панель с черновиком условий и закреплёнными кнопками.
 * UI-редакторы переиспользуются между открытиями; расчёты вариантов выполняются фоновым кешем.
 * Счётчик запросов не позволяет устаревшему результату заменить более свежий.
 */
@Slf4j
public class FilterDrawer extends StackPane {

    private final FilterCatalog catalog;
    private final FilterState state;
    private final FilterEditorRegistry registry;
    private final Supplier<List<?>> allRows;
    private final Runnable applied;
    private final Runnable closed;
    private final VBox panel = new VBox();
    private final VBox fields = new VBox();
    private final ScrollPane scroll = new ScrollPane(fields);
    private final Label error = new Label();
    private final Label loading = new Label("Подготовка фильтров…");
    private final Button apply = new Button("Применить");
    private final Map<FilterField, FilterEditor> editors = new LinkedHashMap<>();
    private final Map<String, Boolean> expanded = new LinkedHashMap<>();
    private final Map<String, VBox> sections = new LinkedHashMap<>();
    private final Map<String, Button> headings = new LinkedHashMap<>();
    private final FilterOptionCache cache;
    private List<?> sourceRows;
    private long optionsRequest;
    private TranslateTransition transition;
    private boolean closing;
    private boolean restoring = true;
    private boolean disposed;

    /**
     * Создаёт панель, редакторы и кеш; UI-узлы принадлежат JavaFX-потоку.
     * @param catalog каталог доступных фильтров
     * @param state применённые условия и черновик
     * @param registry фабрики редакторов
     * @param allRows поставщик полного каталога книг
     * @param applied обновление таблицы после применения
     * @param closed действие возврата фокуса после закрытия
     */
    public FilterDrawer(
        FilterCatalog catalog,
        FilterState state,
        FilterEditorRegistry registry,
        Supplier<List<?>> allRows,
        Runnable applied,
        Runnable closed
    ) {
        this.catalog = catalog;
        this.state = state;
        this.registry = registry;
        this.allRows = allRows;
        this.applied = applied;
        this.closed = closed;
        setId("filterEditor");
        setVisible(false);
        setManaged(false);
        Region backdrop = new Region();
        backdrop.getStyleClass().add("filter-backdrop");
        backdrop.setOnMouseClicked(event -> cancel());
        panel.setId("filterPanel");
        panel.getStyleClass().add("filter-panel");
        panel.setMinWidth(0);
        panel.setPrefWidth(380);
        panel.maxWidthProperty().bind(javafx.beans.binding.Bindings.min(380, widthProperty()));
        panel.setMaxHeight(Double.MAX_VALUE);
        StackPane.setAlignment(panel, Pos.CENTER_RIGHT);
        Label title = new Label("Фильтры");
        title.getStyleClass().add("filter-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = SageTheme.iconButton("close", "Закрыть фильтры без применения", this::cancel);
        close.setId("closeFiltersButton");
        HBox header = new HBox(8, title, spacer, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("filter-header");
        scroll.setId("filterScroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("filter-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        fields.getStyleClass().add("filter-fields");
        loading.setId("filterLoading");
        loading.getStyleClass().add("caption");
        loading.setVisible(false);
        loading.setManaged(false);
        error.setId("filterError");
        error.setWrapText(true);
        error.getStyleClass().add("error-message");
        error.setMinHeight(32);
        error.setMaxWidth(Double.MAX_VALUE);
        Button cancel = new Button("Отмена");
        cancel.setId("cancelFiltersButton");
        cancel.setOnAction(event -> cancel());
        apply.setId("applyFiltersButton");
        apply.getStyleClass().add("primary");
        apply.setOnAction(event -> apply());
        HBox actions = new HBox(8, cancel, apply);
        actions.setAlignment(Pos.CENTER_RIGHT);
        StackPane status = new StackPane(error, loading);
        status.setAlignment(Pos.CENTER_LEFT);
        status.setMinHeight(32);
        VBox footer = new VBox(8, status, actions);
        footer.setId("filterFooter");
        footer.getStyleClass().add("filter-footer");
        panel.getChildren().addAll(header, scroll, footer);
        getChildren().addAll(backdrop, panel);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleTab);
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                ComboBox<?> popup = panel.lookupAll(".combo-box").stream()
                    .filter(node -> node instanceof ComboBox<?> combo && combo.isShowing())
                    .map(node -> (ComboBox<?>) node)
                    .findFirst().orElse(null);
                if (popup != null) {
                    popup.hide();
                } else {
                    cancel();
                }
                event.consume();
            }
        });
        initializeEditors();
        cache = new FilterOptionCache(catalog, editors.entrySet().stream()
            .filter(entry -> entry.getValue().requiresOptions())
            .map(Map.Entry::getKey)
            .toList());
        restoring = false;
    }

    /**
     * Проверяет, открыта ли панель и не началась ли анимация закрытия.
     * @return доступен ли редактируемый черновик пользователю
     */
    public boolean isOpen() {
        return isVisible() && !closing;
    }

    /**
     * Открывает панель с копией применённых условий и готовыми редакторами.
     * Фоновые результаты подставляются отдельно, не блокируя анимацию.
     */
    public void open() {
        if (disposed) {
            return;
        }
        stopTransition();
        closing = false;
        state.begin();
        error.setText("");
        restoring = true;
        for (Map.Entry<FilterField, FilterEditor> entry : editors.entrySet()) {
            FilterField field = entry.getKey();
            FilterCondition condition = state.draft().get(field.id());
            if (!entry.getValue().restore(condition)) {
                FilterEditor editor = registry.create(field, condition, this::refreshDraft);
                editor.node().setId("filter-" + field.id());
                sections.get(field.id()).getChildren().setAll(editor.node());
                entry.setValue(editor);
            }
            entry.getValue().node().getStyleClass().remove("invalid");
        }
        restoring = false;
        setManaged(true);
        setVisible(true);
        toFront();
        apply.setDefaultButton(true);
        requestOptions();
        scroll.setVvalue(0);
        panel.setTranslateX(380);
        animate(0, () -> {
            if (isOpen()) {
                headings.values().stream().findFirst().ifPresent(Node::requestFocus);
            }
        });
    }

    private void initializeEditors() {
        String group = "";
        for (FilterField field : catalog.fields()) {
            if (!field.metadata().group().isBlank() && !group.equals(field.metadata().group())) {
                group = field.metadata().group();
                Label groupLabel = new Label(group);
                groupLabel.getStyleClass().add("filter-group-label");
                fields.getChildren().add(groupLabel);
            }
            FilterEditor editor = registry.create(field, state.draft().get(field.id()), this::refreshDraft);
            editor.node().setId("filter-" + field.id());
            editors.put(field, editor);
            VBox content = new VBox(editor.node());
            sections.put(field.id(), content);
            Button heading = new Button(field.label());
            heading.setId("filter-" + field.id() + "-heading");
            heading.getStyleClass().add("filter-heading");
            heading.setMaxWidth(Double.MAX_VALUE);
            heading.setOnAction(event -> setExpanded(field, !content.isManaged()));
            headings.put(field.id(), heading);
            VBox section = new VBox(4, heading, content);
            section.getStyleClass().add("filter-section");
            fields.getChildren().add(section);
            setExpanded(field, expanded.getOrDefault(field.id(), true));
        }
    }

    /**
     * Отбрасывает черновик и закрывает панель, сохраняя применённые условия.
     */
    public void cancel() {
        if (isOpen()) {
            state.begin();
            close();
        }
    }

    /**
     * Захватывает значения обновлённого каталога и запускает фоновый прогрев кеша.
     * Вызывается и при закрытой панели после создания, изменения или удаления книги.
     */
    public void refreshOptions() {
        if (disposed) {
            return;
        }
        optionsRequest++;
        List<?> rows = allRows.get();
        if (rows != sourceRows) {
            sourceRows = rows;
            cache.replaceRows(rows);
        }
        requestOptions();
    }

    /**
     * Отменяет незавершённые запросы и освобождает фоновый кеш при закрытии главного окна.
     */
    public void dispose() {
        disposed = true;
        optionsRequest++;
        stopTransition();
        cache.close();
        sourceRows = null;
        editors.clear();
        sections.clear();
        headings.clear();
        expanded.clear();
        fields.getChildren().clear();
    }

    private void requestOptions() {
        if (disposed || sourceRows == null) {
            return;
        }
        long request = ++optionsRequest;
        loading.setVisible(true);
        loading.setManaged(true);
        cache.options(isOpen() ? state.draft() : state.applied()).whenComplete((options, failure) -> {
            Runnable update = () -> acceptOptions(request, options, failure);
            if (Platform.isFxApplicationThread()) {
                update.run();
            } else {
                Platform.runLater(update);
            }
        });
    }

    private void acceptOptions(long request, Map<String, List<FilterOption>> options, Throwable failure) {
        if (disposed || request != optionsRequest) {
            return;
        }
        loading.setVisible(false);
        loading.setManaged(false);
        if (failure != null) {
            log.error("Unable to prepare filter options", failure);
            error.setText("Не удалось подготовить значения фильтров");
            return;
        }
        editors.forEach((field, editor) -> {
            if (editor.requiresOptions()) {
                editor.refresh(options.getOrDefault(field.id(), List.of()));
            }
        });
    }

    private void refreshDraft() {
        if (restoring || disposed) {
            return;
        }
        for (Map.Entry<FilterField, FilterEditor> entry : editors.entrySet()) {
            try {
                state.setDraft(entry.getKey().id(), entry.getValue().condition());
                entry.getValue().node().getStyleClass().remove("invalid");
            } catch (IllegalArgumentException e) {
                state.setDraft(entry.getKey().id(), null);
            }
        }
        error.setText("");
        requestOptions();
    }

    private void apply() {
        if (!isOpen()) {
            return;
        }
        for (Map.Entry<FilterField, FilterEditor> entry : editors.entrySet()) {
            FilterField field = entry.getKey();
            try {
                FilterCondition condition = entry.getValue().condition();
                state.setDraft(field.id(), field.processor().canonicalize(field, condition));
            } catch (IllegalArgumentException e) {
                error.setText(field.label() + ": " + e.getMessage());
                error.setAccessibleText(error.getText());
                setExpanded(field, true);
                if (!entry.getValue().node().getStyleClass().contains("invalid")) {
                    entry.getValue().node().getStyleClass().add("invalid");
                }
                double maximum = Math.max(1, fields.getHeight() - scroll.getViewportBounds().getHeight());
                scroll.setVvalue(Math.min(1, headings.get(field.id()).getParent().getLayoutY() / maximum));
                headings.get(field.id()).requestFocus();
                return;
            }
        }
        state.apply();
        applied.run();
        close();
    }

    private void setExpanded(FilterField field, boolean value) {
        expanded.put(field.id(), value);
        VBox content = sections.get(field.id());
        content.setVisible(value);
        content.setManaged(value);
        LineIcon chevron = new LineIcon("down");
        chevron.setRotate(value ? 180 : 0);
        Button heading = headings.get(field.id());
        heading.setGraphic(chevron);
        heading.setAccessibleText(field.label() + (value ? ", свернуть" : ", развернуть"));
    }

    private void close() {
        closing = true;
        apply.setDefaultButton(false);
        stopTransition();
        closed.run();
        animate(380, () -> {
            setVisible(false);
            setManaged(false);
            closing = false;
        });
        requestOptions();
    }

    private void animate(double destination, Runnable finished) {
        if (Boolean.getBoolean("booklibrary.reducedMotion")) {
            panel.setTranslateX(destination);
            finished.run();
            return;
        }
        transition = new TranslateTransition(Duration.millis(140), panel);
        transition.setToX(destination);
        transition.setOnFinished(event -> finished.run());
        transition.play();
    }

    private void stopTransition() {
        if (transition != null) {
            transition.stop();
        }
    }

    private void handleTab(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB || !isOpen()) {
            return;
        }
        List<Node> targets = new ArrayList<>();
        collectFocusTargets(panel, targets);
        if (!targets.isEmpty()) {
            Node focused = getScene().getFocusOwner();
            int index = targets.indexOf(focused);
            if (index < 0 && focused != null) {
                for (Node parent = focused.getParent(); parent != null && index < 0; parent = parent.getParent()) {
                    index = targets.indexOf(parent);
                }
            }
            if (index < 0) {
                index = event.isShiftDown() ? 0 : -1;
            }
            int next = Math.floorMod(index + (event.isShiftDown() ? -1 : 1), targets.size());
            Node target = targets.get(next);
            target.requestFocus();
            reveal(target);
        }
        event.consume();
    }

    private void reveal(Node target) {
        for (Node parent = target; parent != null; parent = parent.getParent()) {
            if (parent == fields) {
                Bounds bounds = fields.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
                double viewport = scroll.getViewportBounds().getHeight();
                double maximum = Math.max(1, fields.getHeight() - viewport);
                double top = scroll.getVvalue() * maximum;
                double destination = bounds.getMinY() < top ? bounds.getMinY()
                    : bounds.getMaxY() > top + viewport ? bounds.getMaxY() - viewport : top;
                scroll.setVvalue(Math.max(0, Math.min(1, destination / maximum)));
                return;
            }
        }
    }

    private void collectFocusTargets(Node node, List<Node> targets) {
        if (!node.isVisible() || !node.isManaged() || node.isDisabled()) {
            return;
        }
        if (node.isFocusTraversable()) {
            targets.add(node);
        }
        if (node instanceof ScrollPane pane) {
            collectFocusTargets(pane.getContent(), targets);
        } else if (node instanceof Parent parent && !(node instanceof Control)) {
            parent.getChildrenUnmodifiable().forEach(child -> collectFocusTargets(child, targets));
        }
    }
}
