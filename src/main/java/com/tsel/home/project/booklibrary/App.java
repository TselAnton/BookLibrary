package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.controller.MainViewController;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            MainViewController mainViewController = new MainViewController();
            mainViewController.startScene(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
            //todo: log it
        }

    }
}
