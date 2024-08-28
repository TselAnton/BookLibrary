package com.tsel.home.project.booklibrary.controller.impl.book;

import static com.tsel.home.project.booklibrary.utils.CollectionUtils.isNotEmpty;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.utils.table.ButtonAnswer;
import java.text.DecimalFormat;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class BookInfoViewController extends AbstractViewController {

    private static final double NORMAL_FONT_SIZE = 18f;
    private static final double SMALL_FONT_SIZE = 14f;

    // Основные элементы

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private Button deleteBookButton;

    @FXML
    private Button editBookButton;

    @FXML
    private Button viewAudioBookSitesButton;

    // Элементы отображения

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label publisherLabel;

    @FXML
    private Label pageCountLabel;

    @FXML
    private CheckBox readCheckBox;

    @FXML
    private CheckBox isHardCoverCheckBox;

    @FXML
    private CheckBox autographCheckBox;

    @FXML
    private Label cycleLabel;

    @FXML
    private CheckBox cycleEndedCheckBox;

    @FXML
    private ImageView coverImage;

    // Заголовки левой части отображения, которые необходимо скрывать

    @FXML
    private Label cycleTitle;

    @FXML
    private Label cycleEndedTitle;


    private Book bookForView;

    @Override
    public void initController(FXMLLoader loader, AbstractViewController parentController, Object... initParameters) {
        UUID bookId = (UUID) initParameters[0];
        if (bookId == null) {
            throw new IllegalStateException("Entity ID for view controller is not passed!");
        }
        this.bookForView = BOOK_REPOSITORY_V2.getById(bookId);
        if (this.bookForView == null) {
            throw new IllegalStateException(format("Not found book by ID '%s' for book info controller", bookId));
        }

        updateBookViewInfo();
    }

    @Override
    public void updateControllerState(UUID bookId) {
        this.bookForView = BOOK_REPOSITORY_V2.getById(bookId);
        if (this.bookForView == null) {
            throw new IllegalStateException(format("Not found book by ID '%s' for book info controller", bookId));
        }

        updateBookViewInfo();
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

    @FXML
    public void editBook() {
        loadModalView(
            "Edit book", "view/edit-view.fxml",
            165,
            -25,
            bookForView.getId()
        );
        updateBookViewInfo();
    }

    @FXML
    public void audioBookSitesView() {
        loadModalView(
            "Audio Book Sites", "view/audio-book-sites-connections-view.fxml",
            165,
            0,
            bookForView.getAudioBookSiteIds()
        );
        updateBookViewInfo();
    }

    @FXML
    public void deleteBook() {
        ButtonAnswer answer = riseAlert(
            CONFIRMATION,
            "Внимание!",
            "Вы уверены?",
            "Книга будет безвозвратно удалена из библиотеки"
        );

        if (answer.isOkAnswer()) {
            BOOK_REPOSITORY_V2.delete(bookForView);

            Stage stage = (Stage) deleteBookButton.getScene().getWindow();
            stage.close();
        }
    }

    private void updateBookViewInfo() {
        setTextWithSizeControl(nameLabel, bookForView.getName());
        setTextWithSizeControl(authorLabel,
            ofNullable(AUTHOR_REPOSITORY_V2.getById(bookForView.getAuthorId()))
                .map(Author::getName)
                .orElse(null)
        );
        setTextWithSizeControl(publisherLabel,
            ofNullable(PUBLISHER_REPOSITORY_V2.getById(bookForView.getPublisherId()))
                .map(Publisher::getName)
                .orElse(null)
        );

        pageCountLabel.setText(String.valueOf(bookForView.getPages()));
        priceLabel.setText(resolvePrice(bookForView.getPrice()));

        readCheckBox.setSelected(bookForView.getRead());
        autographCheckBox.setSelected(Boolean.TRUE.equals(bookForView.getAutograph()));
        isHardCoverCheckBox.setSelected(Boolean.TRUE.equals(bookForView.getHardCover()));

        if (bookForView.getCycleId() != null && CYCLE_REPOSITORY_V2.existById(bookForView.getCycleId())) {
            Cycle cycle = CYCLE_REPOSITORY_V2.getById(bookForView.getCycleId());

            setTextWithSizeControl(cycleLabel, cycle.getName());
            cycleLabel.setText(format("%s (%d / %d)", cycle.getName(), bookForView.getNumberInSeries(), cycle.getBooksInCycle()));
            cycleEndedCheckBox.setSelected(cycle.getEnded());

            cycleTitle.setVisible(true);
            cycleLabel.setVisible(true);
            cycleEndedTitle.setVisible(true);
            cycleEndedCheckBox.setVisible(true);

        } else {
            cycleTitle.setVisible(false);
            cycleLabel.setVisible(false);
            cycleEndedTitle.setVisible(false);
            cycleEndedCheckBox.setVisible(false);
        }

        coverImage.setImage(IMAGE_PROVIDER.resolveCover(bookForView));
        viewAudioBookSitesButton.setVisible(isNotEmpty(bookForView.getAudioBookSiteIds()));
    }

    private String resolvePrice(Double price) {
        if (price == null) return "Бесценно";
        if (price == 0.0) return "Подарок";
        return new DecimalFormat("###,###").format(price) + " руб.";
    }

    private void setTextWithSizeControl(Label label, String text) {
        Font labelFont = label.getFont();
        if (isNotBlank(text) && text.length() > 34) {
            label.setFont(new Font(labelFont.getName(), SMALL_FONT_SIZE));
        } else {
            label.setFont(new Font(labelFont.getName(), NORMAL_FONT_SIZE));
        }
        label.setText(text);
    }
}
