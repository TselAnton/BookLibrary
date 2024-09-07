package com.tsel.home.project.booklibrary.converter;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;
import static com.tsel.home.project.booklibrary.utils.CollectionUtils.isNotEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.CheckBox;

public class BookConverter implements Converter<Book, BookDTO> {

    private static final CycleRepository CYCLE_REPOSITORY_V2 = getBean(CycleRepository.class);
    private static final AuthorRepository AUTHOR_REPOSITORY_V2 = getBean(AuthorRepository.class);
    private static final PublisherRepository PUBLISHER_REPOSITORY_V2 = getBean(PublisherRepository.class);
    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY_V2 = getBean(AudioBookSiteRepository.class);

    @Override
    public BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;
        Boolean isCycleEnded = null;

        if (book.getCycleId() != null && CYCLE_REPOSITORY_V2.existById(book.getCycleId())) {
            Cycle cycle = CYCLE_REPOSITORY_V2.getById(book.getCycleId());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
            isCycleEnded = cycle.getEnded();
        }

        return BookDTO.builder()
            .id(book.getId())
            .name(book.getName())
            .author(getAuthorName(book))
            .publisher(getPublisherName(book))
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
        return ofNullable(AUTHOR_REPOSITORY_V2.getById(book.getAuthorId()))
            .map(Author::getName)
            .orElse(null);
    }

    private String getPublisherName(Book book) {
        return ofNullable(PUBLISHER_REPOSITORY_V2.getById(book.getPublisherId()))
            .map(Publisher::getName)
            .orElse(null);
    }

    private List<String> getAudioBookSiteNames(Book book) {
        return ofNullable(book.getAudioBookSiteIds())
            .orElse(Collections.emptyList())
            .stream()
            .map(AUDIO_BOOK_SITE_REPOSITORY_V2::getById)
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
