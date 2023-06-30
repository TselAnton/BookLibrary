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

    private static final String REVERSE_ORDER = "n";
    private static final String READ_SEARCH_KEY = "read";
    private static final String ENDED_CYCLE_SEARCH_KEY = "end";
    private static final String AUTOGRAPH_SEARCH_KEY = "sign";
    private static final String PRICE_SEARCH_KEY = "price";
    private static final String LESS_SEARCH_KEY = "<";
    private static final String EQUALS_SEARCH_KEY = "=";
    private static final String MORE_SEARCH_KEY = ">";

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
                || searchByKeyWord(searchQuery, bookDTO.getCycleEnded(), ENDED_CYCLE_SEARCH_KEY)
                || searchByAutograph(searchQuery, bookDTO.isAutograph())
                || searchByPrice(searchQuery, bookDTO.getPrice());
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

    private boolean searchByAutograph(String searchQuery, boolean boolValue) {
        if (AUTOGRAPH_SEARCH_KEY.equals(searchQuery) || AUTOGRAPH_SEARCH_KEY.equals(searchQuery.substring(1))) {
            return searchQuery.startsWith(REVERSE_ORDER) != boolValue;
        }
        return false;
    }

    private boolean searchByPrice(String searchQuery, Double price) {
        if (!searchQuery.contains(PRICE_SEARCH_KEY)) {
            return false;
        }

        if (searchQuery.startsWith(REVERSE_ORDER)) {
            return searchQuery.endsWith(PRICE_SEARCH_KEY) && price == null;
        }

        if (price == null) {
            return false;
        }

        if (searchQuery.startsWith(PRICE_SEARCH_KEY)) {
            String querySecondHalf = searchQuery.replace(PRICE_SEARCH_KEY, "");
            String symbol = querySecondHalf.substring(0, 1);
            Double parsedPrice = parseDouble(querySecondHalf.substring(1));
            if (parsedPrice == null) {
                return false;
            }

            return switch (symbol) {
                case LESS_SEARCH_KEY -> price < parsedPrice;
                case EQUALS_SEARCH_KEY -> price.equals(parsedPrice);
                case MORE_SEARCH_KEY -> price > parsedPrice;
                default -> false;
            };
        }

        return false;
    }

    private Double parseDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
