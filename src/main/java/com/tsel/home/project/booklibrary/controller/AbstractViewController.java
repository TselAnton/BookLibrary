package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.converter.BookConverter;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepositoryV2;
import com.tsel.home.project.booklibrary.utils.elements.ButtonAnswer;
import com.tsel.home.project.booklibrary.utils.elements.ImageProvider;
import java.util.UUID;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractViewController {

    public static final String RESOURCE_PATH = "/com/tsel/home/project/booklibrary/";

    protected static final Logger LOGGER = LogManager.getLogger(AbstractViewController.class);

    @Deprecated
    protected static final BookRepository BOOK_REPOSITORY = BookRepository.getInstance();
    @Deprecated
    protected static final CycleRepository CYCLE_REPOSITORY = CycleRepository.getInstance();
    @Deprecated
    protected static final AuthorRepository AUTHOR_REPOSITORY = AuthorRepository.getInstance();
    @Deprecated
    protected static final PublisherRepository PUBLISHER_REPOSITORY = PublisherRepository.getInstance();
    @Deprecated
    protected static final UserSettingsRepository USER_SETTINGS_REPOSITORY = UserSettingsRepository.getInstance();

    protected static final BookRepositoryV2 BOOK_REPOSITORY_V2 = BookRepositoryV2.INSTANCE;
    protected static final CycleRepositoryV2 CYCLE_REPOSITORY_V2 = CycleRepositoryV2.INSTANCE;
    protected static final AuthorRepositoryV2 AUTHOR_REPOSITORY_V2 = AuthorRepositoryV2.INSTANCE;
    protected static final PublisherRepositoryV2 PUBLISHER_REPOSITORY_V2 = PublisherRepositoryV2.INSTANCE;
    protected static final UserSettingsRepositoryV2 USER_SETTINGS_REPOSITORY_V2 = UserSettingsRepositoryV2.INSTANCE;

    protected static final ImageProvider IMAGE_PROVIDER = ImageProvider.INSTANCE;
    protected static final BookConverter BOOK_CONVERTER = new BookConverter();

    /**
     * @return {@link AnchorPane} текущего окна
     */
    protected abstract AnchorPane getMainAnchorPane();

    /**
     * Отображение сообщения об ошибке
     * @param alertType Тип алерта
     * @param title Заголовок
     * @param warnMsg Сообщение в верхней части ошибки
     * @param explanationMsg Сообщение в основной части ошибки
     * @return Результат вызова
     */
    protected ButtonAnswer riseAlert(Alert.AlertType alertType, String title, String warnMsg, String explanationMsg) {
        Alert warnAlert = new Alert(alertType);
        warnAlert.setTitle(title);
        warnAlert.setHeaderText(warnMsg);
        warnAlert.setContentText(explanationMsg);
        return new ButtonAnswer(warnAlert.showAndWait().orElse(null));
    }

    /**
     * Загрузка модального окна из текущего
     *
     * @param modalViewTitle Имя окна
     * @param modalViewResourceFile Ссылка на ресурс
     * @param moveByX Отодвинуть относительно текущего окна по оси X
     * @param moveByY Отодвинуть относительно текущего окна по оси Y
     * @param initParameters Параметры инициализации
     */
    public void loadModalView(
        String modalViewTitle,
        String modalViewResourceFile,
        int moveByX,
        int moveByY,
        Object... initParameters
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + modalViewResourceFile));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(modalViewTitle);
            stage.getIcons().add(IMAGE_PROVIDER.getWindowIcon());
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);

            Stage primaryStage = (Stage) getMainAnchorPane().getScene().getWindow();
            stage.initOwner(primaryStage);

            stage.setX(primaryStage.getX() + moveByX);
            stage.setY(primaryStage.getY() + moveByY);

            AbstractViewController controller = loader.getController();
            controller.initController(loader, this, initParameters);

            stage.showAndWait();

        } catch (Exception e) {
            LOGGER.error("Exception while load model view", e);
        }
    }

    /**
     * Инициализация контроллера. Вызывается из {@link AbstractViewController#loadModalView(String, String, int, int, Object...)}
     * @param parentController Родительский контроллер
     * @param initParameters Параметры инициализации
     */
    public void initController(FXMLLoader loader, AbstractViewController parentController, Object... initParameters) {
        // FOR OVERWRITE
    }

    /**
     * Обновить состояние контроллера. Метод вызывается вручную
     * @param entityId Идентификатор сущности
     */
    public void updateControllerState(UUID entityId) {
        // FOR OVERWRITE
    }
}
