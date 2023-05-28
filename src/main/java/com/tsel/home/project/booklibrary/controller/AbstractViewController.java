package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

import com.tsel.home.project.booklibrary.converter.BookConverter;
import com.tsel.home.project.booklibrary.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.utils.ImageProvider;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractViewController {

    public static final String RESOURCE_PATH = "/com/tsel/home/project/booklibrary/";
    protected static final Logger LOGGER = LogManager.getLogger(AbstractViewController.class);

    protected static final String OK = "OK";

    protected Image iconImage;

    protected final static BookRepository BOOK_REPOSITORY = BookRepository.getInstance();
    protected final static CycleRepository CYCLE_REPOSITORY = CycleRepository.getInstance();
    protected final static AuthorRepository AUTHOR_REPOSITORY = AuthorRepository.getInstance();
    protected final static PublisherRepository PUBLISHER_REPOSITORY = PublisherRepository.getInstance();
    protected final static UserSettingsRepository USER_SETTINGS_REPOSITORY = UserSettingsRepository.getInstance();

    protected final static BookConverter BOOK_CONVERTER = new BookConverter();
    protected final ImageProvider imageProvider = new ImageProvider();

    private final String title;
    private final String resourceFile;

    protected AbstractViewController(String title, String resourceFile) {
        this.title = title;
        this.resourceFile = resourceFile;
        this.iconImage = imageProvider.getDefaultImage();
    }

    public void initController(AbstractViewController parentController, String entityKey) {}

    public void updateControllerState(String entityKey) {}

    public void startScene(Stage stage) {
        try {
            FXMLLoader windowLoader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + resourceFile));
            Scene scene = new Scene(windowLoader.load());

            stage.setTitle(title);
            stage.getIcons().add(iconImage);
            stage.setScene(scene);

            stage.setMinWidth(812);
            stage.setMinHeight(700);

            afterInitScene(windowLoader);

            stage.show();

        } catch (Exception e) {
            LOGGER.error("Exception while start scene", e);
        }
    }

    protected void afterInitScene(FXMLLoader loader) {}

    protected void loadModalView(String modalViewTitle, String modalViewResourceFile, AnchorPane mainStage,
                                 String initEntityKey, AbstractViewController parentViewController,
                                 int moveByX, int moveByY) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + modalViewResourceFile));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(modalViewTitle);
            stage.getIcons().add(iconImage);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);

            Stage primaryStage = (Stage) mainStage.getScene().getWindow();
            stage.initOwner(primaryStage);

            stage.setX(primaryStage.getX() + moveByX);
            stage.setY(primaryStage.getY() + moveByY);

            if (isNotBlank(initEntityKey)) {
                AbstractViewController controller = loader.getController();
                controller.initController(parentViewController, initEntityKey);
            }

            stage.showAndWait();

        } catch (Exception e) {
            LOGGER.error("Exception while load model view", e);
        }
    }

    protected Optional<ButtonType> riseAlert(Alert.AlertType alertType, String title,
                                             String warnMsg, String explanationMsg) {

        Alert warnAlert = new Alert(alertType);
        warnAlert.setTitle(title);
        warnAlert.setHeaderText(warnMsg);
        warnAlert.setContentText(explanationMsg);
        return warnAlert.showAndWait();
    }
}
