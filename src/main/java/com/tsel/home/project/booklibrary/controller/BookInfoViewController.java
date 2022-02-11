package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

public class BookInfoViewController extends AbstractViewController {

    private static final Logger LOGGER = LogManager.getLogger(BookInfoViewController.class);

    @FXML
    private AnchorPane mainPane;

    @FXML
    private Label nameLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label publisherLabel;

    @FXML
    private Label pageCountLabel;

    @FXML
    private Label shelfLabel;

    @FXML
    private CheckBox readCheck;

    @FXML
    private Label cycleLabel;

    @FXML
    private CheckBox cycleEnded;

    @FXML
    private ImageView coverImage;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editBookButton;

    private Book book;

    private Image defaultImg;

    public BookInfoViewController() {
        super(null, null);

        try {
            InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + "default.png");
            defaultImg = new Image(requireNonNull(imageInputStream));

        } catch (Exception e) {
            LOGGER.error("Exception while init BookInfoViewController", e);
        }
    }

    @Override
    public void initController(AbstractViewController parentController, String bookKey) {
        book = BOOK_REPOSITORY.getByName(bookKey);
        if (book == null) {
            throw new IllegalStateException(format("Not found book by key = %s for book info controller", bookKey));
        }

        updateView();
    }

    @Override
    public void updateControllerState(String bookKey) {
        book = BOOK_REPOSITORY.getByName(bookKey);
        if (book == null) {
            throw new IllegalStateException(format("Not found book by key = %s for book info controller", bookKey));
        }

        updateView();
    }

    @FXML
    public void stageKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Закрыть окно?", "");

            if (answer.isPresent() && OK.equals(answer.get().getText())) {
                Stage stage = (Stage) editBookButton.getScene().getWindow();
                stage.close();
            }
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            editBook();
        }
    }

    private void updateView() {
        nameLabel.setText(book.getName());
        authorLabel.setText(book.getAuthor());
        publisherLabel.setText(book.getPublisher());
        pageCountLabel.setText(String.valueOf(book.getPages()));
        shelfLabel.setText(String.valueOf(book.getBookshelf()));

        readCheck.setSelected(book.getRead());

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = CYCLE_REPOSITORY.getByName(book.getCycleName());

            cycleLabel.setText(format("%s (%d / %d)", cycle.getName(), book.getNumberInSeries(), cycle.getBooksInCycle()));
            cycleEnded.setSelected(cycle.getEnded());
        }

        coverImage.setImage(resolveCover(book));
    }

    private Image resolveCover(Book book) {
        if (isNotBlank(book.getCoverImgAbsolutePath())) {
            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try (InputStream inputStream = Files.newInputStream(imgPath)) {
                    Image bookImage = new Image(inputStream);
                    return bookImage.isError()
                            ? defaultImg
                            : bookImage;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return defaultImg;
    }

    @FXML
    public void editBook() {
        loadModalView("Edit book", "edit-view.fxml", mainPane, book.getKey(),
                this, 0, -25);
        updateView();
    }

    @FXML
    public void deleteBook() {
        Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Вы уверены?",
                "Книга будет безвозратно удалена из библиотеки");

        if (answer.isPresent() && OK.equals(answer.get().getText())) {
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            BOOK_REPOSITORY.delete(book);
            stage.close();
        }
    }
}
