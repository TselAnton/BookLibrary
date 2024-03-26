package com.tsel.home.project.booklibrary.converter;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import java.util.Locale;
import javafx.scene.control.CheckBox;

public class BookConverter implements Converter<Book, BookDTO> {

    private final CycleRepository cycleRepository = CycleRepository.getInstance();

    @Override
    public BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;
        Boolean isCycleEnded = null;

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = cycleRepository.getByName(book.getCycleName());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
            isCycleEnded = cycle.getEnded();
        }

        return BookDTO.builder()
                .name(book.getName())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .cycleName(cycleName)
                .cycleNumber(cycleNumber)
                .cycleEnded(getCheckBox(isCycleEnded))
                .read(getCheckBox(book.getRead()))
                .autograph(getCheckBox(book.getAutograph()))
                .pages(book.getPages())
                .cover(book)
                .price(book.getPrice())
                .hardCover(getCheckBox(book.getHardCover()))
                .audiobookSites(book.getAudiobookSites())
                .hasAnyAudioBookSite(getCheckBox(hasAnyAudiBookSites(book)))
                .build();
    }

    private boolean hasAnyAudiBookSites(Book book) {
        return book.getAudiobookSites() != null
            && !book.getAudiobookSites().isEmpty();
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

    @Override
    public String buildEntityKeyByDTO(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }

         StringBuilder compositeKey = new StringBuilder();
         compositeKey.append(bookDTO.getName().replaceAll(" ", "_").toLowerCase(Locale.ROOT));
         compositeKey.append("_");
         compositeKey.append(bookDTO.getAuthor().replaceAll(" ", "_").toLowerCase(Locale.ROOT));
         compositeKey.append("_");
         compositeKey.append(bookDTO.getPublisher().replaceAll(" ", "_").toLowerCase(Locale.ROOT));

         if (isNotBlank(bookDTO.getCycleName())) {
             compositeKey.append("_");
             compositeKey.append(bookDTO.getCycleName().replaceAll(" ", "_").toLowerCase(Locale.ROOT));
         }

         if (isNotBlank(bookDTO.getCycleNumber())) {
             String bookNumberInBook = bookDTO.getCycleNumber().substring(0, bookDTO.getCycleNumber().indexOf("/"));

             compositeKey.append("_");
             compositeKey.append(bookNumberInBook);
         }

         return compositeKey.toString();
    }
}
