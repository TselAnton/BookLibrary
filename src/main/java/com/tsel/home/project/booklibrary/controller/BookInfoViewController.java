package com.tsel.home.project.booklibrary.controller;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import java.text.DecimalFormat;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookInfoViewController extends AbstractViewController {

    private static final Logger LOGGER = LogManager.getLogger(BookInfoViewController.class);

    private static final double NORMAL_FONT_SIZE = 18f;
    private static final double SMALL_FONT_SIZE = 14f;

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
    private CheckBox readCheck;

    @FXML
    private CheckBox autographCheck;

    @FXML
    private Label cycleTitle;

    @FXML
    private Label cycleLabel;

    @FXML
    private Label cycleEndedTitle;

    @FXML
    private CheckBox cycleEnded;

    @FXML
    private ImageView coverImage;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editBookButton;

    @FXML
    private Button viewAudioBookSites;

    @FXML
    private Label priceLabel;

    @FXML
    private CheckBox isHardCoverCheckBox;

    private Book book;

    public BookInfoViewController() {
        super(null, null);
    }

    @Override
    public void initController(AbstractViewController parentController, Object... initParameters) {
        String bookKey = (String) initParameters[0];
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
            Stage stage = (Stage) editBookButton.getScene().getWindow();
            stage.close();

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            editBook();
        }
    }

    private void updateView() {
        updateFontSizeForLongNames(book.getName(), nameLabel);
        updateFontSizeForLongNames(book.getAuthor(), authorLabel);
        updateFontSizeForLongNames(book.getPublisher(), publisherLabel);

        nameLabel.setText(book.getName());
        authorLabel.setText(book.getAuthor());
        publisherLabel.setText(book.getPublisher());
        pageCountLabel.setText(String.valueOf(book.getPages()));
        priceLabel.setText(resolvePrice(book.getPrice()));

        readCheck.setSelected(book.getRead());
        autographCheck.setSelected(Boolean.TRUE.equals(book.getAutograph()));
        isHardCoverCheckBox.setSelected(Boolean.TRUE.equals(book.getHardCover()));

        if (isNotBlank(book.getCycleName())) {
            updateFontSizeForLongNames(book.getCycleName(), cycleLabel);

            Cycle cycle = CYCLE_REPOSITORY.getByName(book.getCycleName());

            cycleLabel.setText(format("%s (%d / %d)", cycle.getName(), book.getNumberInSeries(), cycle.getBooksInCycle()));
            cycleEnded.setSelected(cycle.getEnded());

            cycleTitle.setVisible(true);
            cycleLabel.setVisible(true);
            cycleEndedTitle.setVisible(true);
            cycleEnded.setVisible(true);

        } else {
            cycleTitle.setVisible(false);
            cycleLabel.setVisible(false);
            cycleEndedTitle.setVisible(false);
            cycleEnded.setVisible(false);
        }

        coverImage.setImage(IMAGE_PROVIDER.resolveCover(book));
        viewAudioBookSites.setVisible(book.getAudiobookSites() != null && !book.getAudiobookSites().isEmpty());
    }

    private String resolvePrice(Double price) {
        if (price == null) return "Бесценно";
        if (price == 0.0) return "Подарок";
        return new DecimalFormat("###,###").format(price) + " руб.";
    }

    private void updateFontSizeForLongNames(String text, Label label) {
        Font labelFont = label.getFont();
        if (isNotBlank(text) && text.length() > 34) {
            label.setFont(new Font(labelFont.getName(), SMALL_FONT_SIZE));
        } else {
            label.setFont(new Font(labelFont.getName(), NORMAL_FONT_SIZE));
        }
    }

    @FXML
    public void editBook() {
        loadModalView(
            "Edit book", "view/edit-view.fxml",
            mainPane,
            this,
            165,
            0,
            book.getKey()
        );
        updateView();
    }

    @FXML
    public void audioBookSitesView() {
        loadModalView(
            "Audio Book Sites", "view/audio-book-sites-connections-view.fxml",
            mainPane,
            this,
            165,
            0,
            book.getAudiobookSites()
        );
        updateView();
    }

    @FXML
    public void deleteBook() {
        Optional<ButtonType> answer = riseAlert(CONFIRMATION, "Внимание!", "Вы уверены?",
                "Книга будет безвозвратно удалена из библиотеки");

        if (answer.isPresent() && OK.equals(answer.get().getText())) {
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            BOOK_REPOSITORY.delete(book);
            stage.close();
        }
    }
}
