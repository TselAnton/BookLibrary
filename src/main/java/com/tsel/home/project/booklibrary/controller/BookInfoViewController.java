package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class BookInfoViewController {

    private static final BookRepository bookRepository = BookRepository.getInstance();

    private static final CycleRepository cycleRepository = CycleRepository.getInstance();

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

    public void initData(String bookKey) {
        book = bookRepository.getByName(bookKey);
        if (book == null) {
            throw new IllegalStateException(format("Not found book by key = %s", bookKey));
        }

        nameLabel.setText(book.getName());
        authorLabel.setText(book.getAuthor());
        publisherLabel.setText(book.getPublisher());
        pageCountLabel.setText(String.valueOf(book.getPages()));
        shelfLabel.setText(String.valueOf(book.getBookshelf()));

        readCheck.setSelected(book.getRead());

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = cycleRepository.getByName(book.getCycleName());

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
                    return new Image(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //todo: return default
        return null;
    }

    @FXML
    public void editBook(ActionEvent actionEvent) {

    }

    @FXML
    public void deleteBook(ActionEvent actionEvent) {
        Alert warnAlert = new Alert(Alert.AlertType.CONFIRMATION);
        warnAlert.setTitle("Внимание!");
        warnAlert.setHeaderText("Вы уверены?");
        warnAlert.setContentText("Книга будет безвозратно удалена из библиотеки");
        Optional<ButtonType> answer = warnAlert.showAndWait();

        if (answer.isPresent() && "OK".equals(answer.get().getText())) {
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            bookRepository.delete(book);
            // todo: Заодно удалять автора и цикл, если таковых не имеется
            stage.close();
        }
    }
}
