package com.tsel.home.project.booklibrary.search;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

public class SearchService {

    public List<BookDTO> search(String searchQuery, List<BookDTO> bookDTOS) {
        if (isBlank(searchQuery)) {
            return bookDTOS;
        }

        String filteredSearchQuery = searchQuery.trim().toLowerCase(Locale.ROOT);

        List<BookDTO> result = new ArrayList<>();
        for (BookDTO dto : bookDTOS) {
            if (isAvailableByQuery(filteredSearchQuery, dto)) {
                result.add(dto);
            }
        }

        return result;
    }

    private boolean isAvailableByQuery(String searchQuery, BookDTO bookDTO) {
        return isContainsValue(searchQuery, bookDTO.getName())
                || isContainsValue(searchQuery, bookDTO.getAuthor())
                || isContainsValue(searchQuery, bookDTO.getShelf())
                || isContainsValue(searchQuery, bookDTO.getCycleName())
                || isContainsValue(searchQuery, bookDTO.getCycleNumber())
                || isContainsBoolean(searchQuery, bookDTO.getRead());
    }

    private boolean isContainsValue(String searchQuery, String field) {
        return isNotBlank(field) && field.toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private boolean isContainsBoolean(String searchQuery, CheckBox checkBox) {
        return "true".equals(searchQuery) && checkBox.isSelected()
                || "false".equals(searchQuery) && !checkBox.isSelected();
    }
}
