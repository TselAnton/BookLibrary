package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.controller.MainViewController;
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
