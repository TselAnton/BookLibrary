package com.tsel.home.project.booklibrary.converter;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static com.tsel.home.project.booklibrary.utils.CollectionUtils.isNotEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.data.*;
import com.tsel.home.project.booklibrary.dao.repository.impl.*;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.CheckBox;

public class BookConverter implements Converter<Book, BookDTO> {

    private final CycleRepository cycleRepository = getBean(CycleRepository.class);
    private final AuthorRepository authorRepository = getBean(AuthorRepository.class);
    private final PublisherRepository publisherRepository = getBean(PublisherRepository.class);
    private final GenreRepository genreRepository = getBean(GenreRepository.class);
    private final AudioBookSiteRepository audioBookSiteRepository = getBean(AudioBookSiteRepository.class);

    @Override
    public BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;
        Boolean isCycleEnded = null;

        if (book.getCycleId() != null && cycleRepository.existById(book.getCycleId())) {
            Cycle cycle = cycleRepository.getById(book.getCycleId());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
            isCycleEnded = cycle.getEnded();
        }

        BookDTO dto = BookDTO.builder()
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

        ImageProvider imageProvider = getBean(ImageProvider.class);
        dto.getCover()
            .imageProperty()
            .addListener((observable, previous, image) ->
                dto.setHasCoverImage(!imageProvider.isPlaceholder(image))
            );
        return dto;
    }

    private String getAuthorName(Book book) {
        return ofNullable(authorRepository.getById(book.getAuthorId()))
            .map(Author::getName)
            .orElse(null);
    }

    private String getPublisherName(Book book) {
        return ofNullable(publisherRepository.getById(book.getPublisherId()))
            .map(Publisher::getName)
            .orElse(null);
    }

    private String getGenreName(Book book) {
        return ofNullable(genreRepository.getById(book.getGenreId()))
            .map(Genre::getName)
            .orElse(null);
    }

    private List<String> getAudioBookSiteNames(Book book) {
        return ofNullable(book.getAudioBookSiteIds())
            .orElse(Collections.emptyList())
            .stream()
            .map(audioBookSiteRepository::getById)
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
