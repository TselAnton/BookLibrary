package com.tsel.home.project.booklibrary.converter;

import com.tsel.home.project.booklibrary.builder.BookDTOBuilder;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import javafx.scene.control.CheckBox;

import java.util.Locale;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;

public class BookConverter {

    private final CycleRepository cycleRepository = CycleRepository.getInstance();

    public BookDTO convert(Book book) {
        String cycleName = null;
        String cycleNumber = null;

        if (isNotBlank(book.getCycleName())) {
            Cycle cycle = cycleRepository.getByName(book.getCycleName());
            cycleName = cycle.getName();
            cycleNumber = format("%d/%d", book.getNumberInSeries(), cycle.getBooksInCycle());
        }

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(book.getRead());
        checkBox.setDisable(true);
        checkBox.getStyleClass().add("disabled-check-box");

        return BookDTOBuilder.builder()
                .name(book.getName())
                .author(book.getAuthor())
                .shelf(String.valueOf(book.getBookshelf()))
                .cycleName(cycleName)
                .cycleNumber(cycleNumber)
                .read(checkBox)
                .build();
    }

     public String buildEntityKeyByDTO(BookDTO bookDTO) {
         StringBuilder compositeKey = new StringBuilder();
         compositeKey.append(bookDTO.getName().replaceAll(" ", "_").toLowerCase(Locale.ROOT));
         compositeKey.append("_");
         compositeKey.append(bookDTO.getAuthor().replaceAll(" ", "_").toLowerCase(Locale.ROOT));

         if (isNotBlank(bookDTO.getCycleNumber())) {
             String bookNumberInBook = bookDTO.getCycleNumber().substring(0, bookDTO.getCycleNumber().indexOf("/"));

             compositeKey.append("_");
             compositeKey.append(bookNumberInBook);
         }

         return compositeKey.toString();
     }
}
