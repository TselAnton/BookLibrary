package com.tsel.home.project.booklibrary;

import static com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2.DEFAULT_REPOSITORY_PATH;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.utils.file.ArchiveStorageFilesUtils;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    public static void main(String[] args) {
        JavaFXRunner.main(args);
    }

    public static class JavaFXRunner extends Application {

        private static final Logger LOGGER = LogManager.getLogger(JavaFXRunner.class);

        public static void main(String[] args) {
            ArchiveStorageFilesUtils.archiveStorages(DEFAULT_REPOSITORY_PATH, Paths.get("", "/backup"));
            launch(args);
        }

        @Override
        public void start(Stage primaryStage) {
            try {
                MainViewController mainViewController = new MainViewController();
                mainViewController.startScene(primaryStage);

            } catch (Exception e) {
                LOGGER.error("Can't start main window", e);
            }
        }
    }
}
