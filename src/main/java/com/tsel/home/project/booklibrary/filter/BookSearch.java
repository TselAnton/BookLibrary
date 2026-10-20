package com.tsel.home.project.booklibrary.filter;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class BookSearch {

    private BookSearch() {}

    public static List<BookDTO> search(List<BookDTO> books, String query) {
        String normalized = normalize(query);
        if (normalized.isEmpty()) {
            return books;
        }
        return books.stream()
            .filter(book -> Stream.of(book.getName(), book.getAuthor(), book.getPublisher(), book.getGenre(), book.getCycleName())
                .anyMatch(value -> normalize(value).contains(normalized)))
            .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
