package com.tsel.home.project.booklibrary.search;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.CheckBox;

public class SearchService {

    private static final String READ_SEARCH_KEY = "read";
    private static final String ENDED_CYCLE_SEARCH_KEY = "end";
    private static final String REVERSE_ORDER = "n";

    public List<BookDTO> search(String searchQuery, List<BookDTO> bookDTOS) {
        if (isBlank(searchQuery)) {
            return bookDTOS.stream()
                    .sorted(Comparator.comparing(BookDTO::getName))
                    .toList();
        }

        String filteredSearchQuery = searchQuery.trim().toLowerCase(Locale.ROOT);

        List<BookDTO> result = new ArrayList<>();
        for (BookDTO dto : bookDTOS) {
            if (isAvailableByQuery(filteredSearchQuery, dto)) {
                result.add(dto);
            }
        }

        return result.stream()
                .sorted(Comparator.comparing(BookDTO::getName))
                .toList();
    }

    private boolean isAvailableByQuery(String searchQuery, BookDTO bookDTO) {
        return isContainsValue(searchQuery, bookDTO.getName())
                || isContainsValue(searchQuery, bookDTO.getAuthor())
                || isContainsValue(searchQuery, bookDTO.getPublisher())
                || isContainsValue(searchQuery, bookDTO.getCycleName())
                || isContainsValue(searchQuery, bookDTO.getCycleNumber())
                || isContainsValue(searchQuery, String.valueOf(bookDTO.getPages()))
                || searchByKeyWord(searchQuery, bookDTO.getRead(), READ_SEARCH_KEY)
                || searchByKeyWord(searchQuery, bookDTO.getCycleEnded(), ENDED_CYCLE_SEARCH_KEY);
    }

    private boolean isContainsValue(String searchQuery, String field) {
        return isNotBlank(field) && field.toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private boolean searchByKeyWord(String searchQuery, CheckBox checkBox, String searchKey) {
        if (searchQuery.contains(searchKey)) {
            if (searchQuery.startsWith(REVERSE_ORDER) && searchKey.equals(searchQuery.substring(1))) {
                return checkBox != null && !checkBox.isSelected();
            } else {
                return searchKey.equals(searchQuery) && checkBox != null && checkBox.isSelected();
            }
        }

        return false;
    }
}
