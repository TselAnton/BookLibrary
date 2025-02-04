package com.tsel.home.project.booklibrary.converter;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static com.tsel.home.project.booklibrary.utils.CollectionUtils.isNotEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.data.*;
import com.tsel.home.project.booklibrary.dao.repository.impl.*;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.CheckBox;

public class BookConverter implements Converter<Book, BookDTO> {

    private static final CycleRepository CYCLE_REPOSITORY = getBean(CycleRepository.class);
    private static final AuthorRepository AUTHOR_REPOSITORY = getBean(AuthorRepository.class);
    private static final PublisherRepository PUBLISHER_REPOSITORY = getBean(PublisherRepository.class);
    private static final GenreRepository GENRE_REPOSITORY = getBean(GenreRepository.class);
    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY = getBean(AudioBookSiteRepository.class);

    @Override
    public BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;
        Boolean isCycleEnded = null;

        if (book.getCycleId() != null && CYCLE_REPOSITORY.existById(book.getCycleId())) {
            Cycle cycle = CYCLE_REPOSITORY.getById(book.getCycleId());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
            isCycleEnded = cycle.getEnded();
        }

        return BookDTO.builder()
            .id(book.getId())
            .name(book.getName())
            .author(getAuthorName(book))
            .publisher(getPublisherName(book))
            .publicationYear(book.getPublicationYear())
            .genre(getGenreName(book))
            .cycleName(cycleName)
            .cycleNumber(cycleNumber)
            .cycleEnded(getCheckBox(isCycleEnded))
            .read(getCheckBox(book.getRead()))
            .autograph(getCheckBox(book.getAutograph()))
            .pages(book.getPages())
            .cover(book)
            .price(book.getPrice())
            .hardCover(getCheckBox(book.getHardCover()))
            .audiobookSites(getAudioBookSiteNames(book))
            .hasAnyAudioBookSite(getCheckBox(isNotEmpty(book.getAudioBookSiteIds())))
            .build();
    }

    private String getAuthorName(Book book) {
        return ofNullable(AUTHOR_REPOSITORY.getById(book.getAuthorId()))
            .map(Author::getName)
            .orElse(null);
    }

    private String getPublisherName(Book book) {
        return ofNullable(PUBLISHER_REPOSITORY.getById(book.getPublisherId()))
            .map(Publisher::getName)
            .orElse(null);
    }

    private String getGenreName(Book book) {
        return ofNullable(GENRE_REPOSITORY.getById(book.getGenreId()))
            .map(Genre::getName)
            .orElse(null);
    }

    private List<String> getAudioBookSiteNames(Book book) {
        return ofNullable(book.getAudioBookSiteIds())
            .orElse(Collections.emptyList())
            .stream()
            .map(AUDIO_BOOK_SITE_REPOSITORY::getById)
            .filter(Objects::nonNull)
            .map(AudioBookSite::getName)
            .toList();
    }

    private CheckBox getCheckBox(Boolean isChecked) {
        if (isChecked == null) {
            return null;
        }

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(isChecked);
        checkBox.setDisable(true);
        checkBox.getStyleClass().add("disabled-check-box");

        return checkBox;
    }
}
