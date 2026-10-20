package com.tsel.home.project.booklibrary.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BookSearchTest {

    private final BookDTO book = BookDTO.builder()
        .name("Зелёная миля")
        .author("Стивен Кинг")
        .publisher("АСТ")
        .genre("Роман")
        .cycleName("Избранное")
        .build();

    @ParameterizedTest
    @ValueSource(strings = {"  ЗЕЛЕНАЯ  ", "кинг", "аст", "роман", "избранное", "милЯ"})
    void findsTextAcrossBookFields(String query) {
        assertEquals(List.of(book), BookSearch.search(List.of(book, BookDTO.builder().build()), query));
    }

    @Test
    void clearingQueryRestoresInputWithoutAddingFilteredBooks() {
        List<BookDTO> filtered = List.of(book);
        assertSame(filtered, BookSearch.search(filtered, " "));
        assertSame(filtered, BookSearch.search(filtered, null));
        assertEquals(List.of(), BookSearch.search(filtered, "неизвестная книга"));
        assertEquals(List.of(), BookSearch.search(List.of(), "Кинг"));
    }
}
