package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;

import com.tsel.home.project.booklibrary.converter.BookConverter;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.utils.table.ButtonAnswer;
import com.tsel.home.project.booklibrary.ui.SageDialogs;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import com.tsel.home.project.booklibrary.ui.WindowPlacement;
import com.tsel.home.project.booklibrary.controller.impl.book.AbstractBookController;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import java.net.URI;
import java.util.UUID;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractViewController {

    public static final URI RESOURCE_PATH = URI.create("/com/tsel/home/project/booklibrary/");

    protected final BookRepository bookRepository = getBean(BookRepository.class);
    protected final CycleRepository cycleRepository = getBean(CycleRepository.class);
    protected final GenreRepository genreRepository = getBean(GenreRepository.class);
    protected final AuthorRepository authorRepository = getBean(AuthorRepository.class);
    protected final PublisherRepository publisherRepository = getBean(PublisherRepository.class);
    protected final UserSettingsRepository userSettingsRepository = getBean(UserSettingsRepository.class);

    protected final ImageProvider imageProvider = getBean(ImageProvider.class);
    protected final BookConverter bookConverter = new BookConverter();

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
        ButtonType accept = new ButtonType(alertType == Alert.AlertType.CONFIRMATION ? "Продолжить" : "Понятно", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        javafx.stage.Window owner = getMainAnchorPane() == null || getMainAnchorPane().getScene() == null
            ? null
            : getMainAnchorPane().getScene().getWindow();
        Alert warnAlert = SageDialogs.create(
            owner, alertType, title, warnMsg, explanationMsg,
            alertType == Alert.AlertType.CONFIRMATION ? new ButtonType[] {accept, cancel} : new ButtonType[] {accept}
        );

        log.info("Raised alert {} with message '{}'", alertType, explanationMsg);
        return new ButtonAnswer(warnAlert.showAndWait().orElse(null));
    }

    /**
     * Закрыть текущее окно
     * @param node Любой элемент текущего окна
     */
    protected void closeStage(Node node) {
        log.info("Close scene");
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
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
            SageTheme.install(stage, scene, modalViewTitle, false);
            stage.getIcons().add(imageProvider.getWindowIcon());
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);

            Stage primaryStage = (Stage) getMainAnchorPane().getScene().getWindow();
            stage.initOwner(primaryStage);

            stage.setX(primaryStage.getX() + moveByX);
            stage.setY(primaryStage.getY() + moveByY);

            AbstractViewController controller = loader.getController();
            controller.initController(loader, this, initParameters);
            if (controller instanceof AbstractBookController bookController) {
                bookController.configureWindow(stage);
                WindowPlacement.positionRelativeToMainWindow(stage);
            } else {
                stage.setX(primaryStage.getX() + Math.max(0, (primaryStage.getWidth() - scene.getRoot().prefWidth(-1)) / 2));
                Rectangle2D bounds = Screen.getScreensForRectangle(
                    primaryStage.getX(),
                    primaryStage.getY(),
                    primaryStage.getWidth(),
                    primaryStage.getHeight()
                )
                    .stream()
                    .findFirst()
                    .orElse(Screen.getPrimary())
                    .getVisualBounds();

                stage.setX(Math.max(bounds.getMinX(), Math.min(stage.getX(), bounds.getMaxX() - scene.getRoot().prefWidth(-1))));
                stage.setY(Math.max(bounds.getMinY(), Math.min(stage.getY(), bounds.getMaxY() - scene.getRoot().prefHeight(-1))));
            }

            log.info("Opening modal window {}", modalViewResourceFile);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Exception while load model view", e);
            riseAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно", "Повторите попытку. Подробности доступны в журнале приложения.");
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
