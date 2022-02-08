package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class MainViewController extends Application {

    private final BookRepository bookRepository = BookRepository.getInstance();

    private Image iconImage = null;

    @FXML
    private AnchorPane mainStage;

    public MainViewController() {
        try {
            iconImage = new Image(requireNonNull(MainViewController.class.getResourceAsStream("icon.png")));
        } catch (Exception e) {
            e.printStackTrace();
            //todo: logger it
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader mainWindowRoot = new FXMLLoader(MainViewController.class.getResource("main-view.fxml"));
        Scene scene = new Scene(mainWindowRoot.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Book library");
        primaryStage.getIcons().add(iconImage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void addBook(ActionEvent actionEvent) throws IOException {
        showModalView("add-view.fxml", "Add new book");

        //todo: update info
    }

    @FXML
    public void editBook(ActionEvent actionEvent) throws IOException {
        showModalView("edit-view.fxm", "Edit book");

        //todo: update info
    }

    private void showModalView(String modalViewName, String title) throws IOException {
        FXMLLoader addWindow = new FXMLLoader(MainViewController.class.getResource(modalViewName));
        Scene secondScene = new Scene(addWindow.load());

        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.getIcons().add(iconImage);
        newWindow.setScene(secondScene);
        newWindow.setResizable(false);
        newWindow.initModality(Modality.WINDOW_MODAL);

        Stage primaryStage = (Stage) mainStage.getScene().getWindow();
        newWindow.initOwner(primaryStage);

        newWindow.setX(primaryStage.getX() + 300);
        newWindow.setY(primaryStage.getY());

        newWindow.show();
    }
}
