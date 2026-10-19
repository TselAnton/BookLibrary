package com.tsel.home.project.booklibrary.controller.impl.book;

import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

@Getter
public class BookInfoViewController extends AbstractViewController {
    @FXML private AnchorPane mainAnchorPane;
    @FXML private Button deleteBookButton;
    @FXML private Button editBookButton;
    @FXML private Label nameLabel;
    @FXML private Label authorLabel;
    @FXML private Label publisherLabel;
    @FXML private Label pageCountLabel;
    @FXML private Label publicationYearLabel;
    @FXML private Label genreLabel;
    @FXML private Label hardCoverLabel;
    @FXML private Label readLabel;
    @FXML private Label autographLabel;
    @FXML private Label priceLabel;
    @FXML private Label cycleLabel;
    @FXML private Label cycleEndedLabel;
    @FXML private ImageView coverImage;
    private Book bookForView;
    private AbstractViewController parentController;

    @Override public void initController(FXMLLoader loader, AbstractViewController parent, Object... parameters) {
        parentController = parent;
        updateControllerState((UUID) parameters[0]);
    }

    @Override public void updateControllerState(UUID id) {
        bookForView = bookRepository.getById(id);
        if (bookForView == null) { closeWindow(); return; }
        updateBookViewInfo();
        if (parentController != null) parentController.updateControllerState(id);
    }

    @FXML public void stageKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) { closeWindow(); event.consume(); }
    }

    @FXML public void closeWindow() { closeStage(mainAnchorPane); }

    @FXML public void editBook() {
        loadModalView("Изменить книгу", "view/edit-view.fxml", 165, -32, bookForView.getId());
        updateControllerState(bookForView.getId());
    }

    @FXML public void deleteBook() {
        if (!riseAlert(Alert.AlertType.CONFIRMATION, "Удаление книги", "Удалить «" + bookForView.getName() + "»?",
            "Книга будет безвозвратно удалена из библиотеки.").isOkAnswer()) return;
        try {
            bookRepository.delete(bookForView);
            closeWindow();
        } catch (RuntimeException e) {
            riseAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить книгу", "Повторите попытку. Подробности доступны в журнале.");
        }
    }

    private void updateBookViewInfo() {
        setValue(nameLabel, bookForView.getName());
        Author author = authorRepository.getById(bookForView.getAuthorId());
        Publisher publisher = publisherRepository.getById(bookForView.getPublisherId());
        Genre genre = genreRepository.getById(bookForView.getGenreId());
        setValue(authorLabel, author == null ? null : author.getName());
        setValue(publisherLabel, publisher == null ? null : publisher.getName());
        setValue(genreLabel, genre == null ? null : genre.getName());
        setValue(pageCountLabel, bookForView.getPages());
        setValue(publicationYearLabel, bookForView.getPublicationYear());
        setValue(hardCoverLabel, yesNo(bookForView.getHardCover()));
        setValue(readLabel, yesNo(bookForView.getRead()));
        setValue(autographLabel, yesNo(bookForView.getAutograph()));
        NumberFormat price = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        price.setMaximumFractionDigits(2);
        setValue(priceLabel, bookForView.getPrice() == null ? null : price.format(bookForView.getPrice()) + " ₽");
        Cycle cycle = cycleRepository.getById(bookForView.getCycleId());
        setValue(cycleLabel, cycle == null ? null : cycle.getName() + " · " + bookForView.getNumberInSeries() + " из " + cycle.getBooksInCycle());
        setValue(cycleEndedLabel, cycle == null ? null : yesNo(cycle.getEnded()));
        coverImage.imageProperty().bind(imageProvider.resolveCover(bookForView).imageProperty());
    }

    private String yesNo(Boolean value) { return Boolean.TRUE.equals(value) ? "Да" : "Нет"; }

    private void setValue(Label label, Object value) {
        String text = value == null ? "—" : value.toString();
        label.setText(text);
        label.setTooltip(new Tooltip(text));
    }
}
