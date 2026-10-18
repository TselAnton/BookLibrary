package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.stage.Stage;

/** Run on the test classpath to inspect a disposable sample catalog. */
public final class DesignPreview {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("booklibrary-design-preview-");
        DesignFixture.initialize(directory);
        Platform.startup(() -> {
            DesignFixture.populate();
            new MainViewController().startScene(new Stage());
        });
    }
}
