package com.tsel.home.project.booklibrary.ui;

import com.tsel.home.project.booklibrary.update.AppVersion;
import com.tsel.home.project.booklibrary.update.GitHubReleaseClient;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StartupUpdateCheck {

    private StartupUpdateCheck() {}

    public static Task<GitHubReleaseClient.Release> start(Window owner, GitHubReleaseClient client) {
        Task<GitHubReleaseClient.Release> task = new Task<>() {
            @Override
            protected GitHubReleaseClient.Release call() throws Exception {
                return client.check(AppVersion.current(), AppVersion.platform());
            }
        };
        ChangeListener<Boolean> visibility = (observable, previous, showing) -> {
            if (!showing) {
                task.cancel(true);
            }
        };
        owner.showingProperty().addListener(visibility);
        task.setOnSucceeded(event -> {
            owner.showingProperty().removeListener(visibility);
            if (task.getValue() != null && owner.isShowing() && !UpdateWindow.isOpen(owner)) {
                new UpdateWindow(owner, client).show(task.getValue());
            }
        });
        task.setOnFailed(event -> {
            owner.showingProperty().removeListener(visibility);
            log.warn("Startup update check failed", task.getException());
        });
        task.setOnCancelled(event -> owner.showingProperty().removeListener(visibility));
        Thread worker = new Thread(task, "booklibrary-startup-update-check");
        worker.setDaemon(true);
        worker.start();
        return task;
    }
}
