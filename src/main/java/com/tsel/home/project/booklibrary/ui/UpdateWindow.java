package com.tsel.home.project.booklibrary.ui;

import com.tsel.home.project.booklibrary.update.AppVersion;
import com.tsel.home.project.booklibrary.update.ApplicationPaths;
import com.tsel.home.project.booklibrary.update.GitHubReleaseClient;
import com.tsel.home.project.booklibrary.update.UpdateLauncher;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class UpdateWindow {

    private final Stage stage = new Stage();
    private final Window owner;
    private final GitHubReleaseClient client;
    private final Label status = new Label();
    private final TextArea notes = new TextArea();
    private final ProgressBar progress = new ProgressBar();
    private final Button action = new Button("Проверить ещё раз");
    private final Button close = new Button("Закрыть");
    private Task<?> currentTask;
    private GitHubReleaseClient.Release release;
    private Path downloaded;
    private Path planFile;
    private boolean installing;

    public UpdateWindow(Window owner, GitHubReleaseClient client) {
        this.owner = owner;
        this.client = client;
        stage.getProperties().put(UpdateWindow.class.getName(), true);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        HBox header = new HBox();
        header.setId("windowHeader");
        header.getStyleClass().add("window-header");
        header.setMinHeight(48);
        status.setId("updateStatus");
        status.setWrapText(true);
        status.setMinHeight(60);
        Label version = new Label("Текущая версия: " + AppVersion.current());
        version.getStyleClass().add("caption");
        progress.setId("updateProgress");
        progress.setMaxWidth(Double.MAX_VALUE);
        notes.setId("updateNotes");
        notes.setEditable(false);
        notes.setWrapText(true);
        notes.setAccessibleText("Изменения в новой версии");
        VBox.setVgrow(notes, Priority.ALWAYS);
        VBox content = new VBox(12, version, status, progress, notes);
        content.setPadding(new Insets(16));
        VBox.setVgrow(content, Priority.ALWAYS);
        action.setId("updateActionButton");
        action.getStyleClass().add("primary");
        close.setCancelButton(true);
        close.setOnAction(event -> SageTheme.requestClose(stage));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(10, spacer, close, action);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("dialog-footer");
        VBox root = new VBox(header, content, footer);
        root.setPrefSize(560, 400);
        Scene scene = new Scene(root);
        SageTheme.install(stage, scene, "Проверить обновления", false);
        stage.setScene(scene);
        stage.getIcons().add(SageTheme.windowIcon());
        stage.setOnCloseRequest(event -> {
            if (installing) {
                event.consume();
            } else if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel(true);
            }
        });
        stage.setOnHidden(event -> {
            if (planFile == null && downloaded != null) {
                try {
                    Files.deleteIfExists(downloaded);
                } catch (Exception e) {
                    log.warn("Can't remove unused update download", e);
                }
            }
        });
    }

    public Stage show() {
        WindowPlacement.positionRelativeToMainWindow(stage);
        stage.show();
        check();
        return stage;
    }

    public Stage show(GitHubReleaseClient.Release available) {
        WindowPlacement.positionRelativeToMainWindow(stage);
        stage.show();
        showRelease(available);
        return stage;
    }

    static boolean isOpen(Window owner) {
        return Window.getWindows().stream().anyMatch(window -> window instanceof Stage child
            && child.getOwner() == owner && Boolean.TRUE.equals(window.getProperties().get(UpdateWindow.class.getName())));
    }

    private void check() {
        notes.clear();
        status.setText("Проверяем стабильные релизы GitHub…");
        Task<GitHubReleaseClient.Release> task = new Task<>() {
            @Override
            protected GitHubReleaseClient.Release call() throws Exception {
                return client.check(AppVersion.current(), AppVersion.platform());
            }
        };
        task.setOnSucceeded(event -> {
            finish();
            showRelease(task.getValue());
        });
        run(task);
    }

    private void showRelease(GitHubReleaseClient.Release available) {
        release = available;
        if (release == null) {
            status.setText("Новых стабильных версий нет.");
            action.setText("Проверить ещё раз");
            action.setOnAction(event -> check());
        } else {
            notes.setText(release.notes());
            status.setText("Доступна версия " + release.version() + ". Размер: " + (release.size() / 1024 / 1024) + " МБ.");
            action.setText("Скачать обновление");
            action.setOnAction(event -> download());
            if (ApplicationPaths.applicationJar() == null) {
                status.setText(status.getText() + " Автоустановка доступна только при запуске собранного JAR.");
                action.setDisable(true);
            }
        }
    }

    private void download() {
        status.setText("Скачиваем обновление во временную папку и проверяем JAR…");
        Task<Path> task = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return client.download(release, value -> updateProgress(value, 1));
            }
        };
        task.setOnSucceeded(event -> {
            downloaded = task.getValue();
            finish();
            status.setText("JAR проверен. Установка создаст резервную копию программы и данных, затем перезапустит приложение.");
            action.setText("Обновить и перезапустить");
            action.setOnAction(actionEvent -> install());
        });
        run(task);
    }

    private void install() {
        ButtonType accept = new ButtonType("Обновить и перезапустить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION,
            "Программа штатно закроется. Помощник сохранит текущий JAR, repository и настройки в backup/updates рядом с программой."
                + "\nПри необходимости Windows запросит права администратора.\nПосле установки новая версия запустится автоматически.", accept, cancel);
        dialog.initOwner(stage);
        dialog.setTitle("Установить версию " + release.version() + "?");
        dialog.setHeaderText(dialog.getTitle());
        SageTheme.apply(dialog.getDialogPane().getScene());
        ((Button) dialog.getDialogPane().lookupButton(accept)).setDefaultButton(false);
        ((Button) dialog.getDialogPane().lookupButton(cancel)).setDefaultButton(true);
        Platform.runLater(() -> dialog.getDialogPane().lookupButton(cancel).requestFocus());
        if (dialog.showAndWait().orElse(cancel) != accept || !closeOtherWindows()) {
            return;
        }
        installing = true;
        close.setDisable(true);
        status.setText("Запускаем помощника. Если появится запрос Windows, разрешите установку…");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (planFile == null) {
                    planFile = UpdateLauncher.prepare(downloaded, release);
                }
                UpdateLauncher.launchAndAwaitReady(planFile);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            stage.close();
            Platform.exit();
        });
        run(task);
    }

    private boolean closeOtherWindows() {
        List<Stage> windows = Window.getWindows().stream().filter(window -> window != owner && window != stage)
            .filter(Stage.class::isInstance).map(Stage.class::cast)
            .filter(window -> depth(window) > 0).sorted(Comparator.comparingInt(this::depth).reversed()).toList();
        for (Stage other : windows) {
            SageTheme.requestClose(other);
            if (other.isShowing()) {
                return false;
            }
        }
        return true;
    }

    private int depth(Window window) {
        int depth = 0;
        while (window instanceof Stage child && child.getOwner() != null) {
            depth++;
            window = child.getOwner();
            if (window == owner) {
                return depth;
            }
        }
        return 0;
    }

    private void run(Task<?> task) {
        currentTask = task;
        action.setDisable(true);
        progress.progressProperty().unbind();
        progress.progressProperty().bind(task.progressProperty());
        task.setOnFailed(event -> {
            log.error("Update operation failed", task.getException());
            installing = false;
            close.setDisable(false);
            finish();
            status.setText("Не удалось выполнить обновление. " + task.getException().getMessage());
            action.setText(downloaded == null ? "Проверить ещё раз" : "Повторить установку");
            action.setOnAction(actionEvent -> {
                if (downloaded == null) {
                    check();
                } else {
                    install();
                }
            });
        });
        Thread worker = new Thread(task, "booklibrary-update");
        worker.setDaemon(true);
        worker.start();
    }

    private void finish() {
        progress.progressProperty().unbind();
        progress.setProgress(0);
        action.setDisable(false);
    }
}
