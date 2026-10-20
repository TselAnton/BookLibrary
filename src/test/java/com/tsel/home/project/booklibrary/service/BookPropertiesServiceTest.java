package com.tsel.home.project.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookPropertiesServiceTest {

    @TempDir
    Path directory;

    @Test
    void explicitDatesOverrideAutomationAndNormalSaveStillTracksChanges() {
        BookRepository books = repository(1000);
        Book book = Book.builder().name("Книга").read(false).authorId(UUID.randomUUID()).publisherId(UUID.randomUUID()).build();
        books.save(book);
        Book original = books.getById(book.getId());
        books = repository(2000);
        Book edited = books.getById(book.getId());
        edited.setRead(true);
        edited.setCreatedAt(100L);
        edited.setUpdatedAt(300L);
        edited.setReadUpdatedAt(200L);
        UUID author = edited.getAuthorId();
        edited.setAuthorId(UUID.randomUUID());
        Book saved = new BookPropertiesService(books).save(original, edited, Set.of("createdAt", "updatedAt", "readUpdatedAt"));
        assertEquals(100L, saved.getCreatedAt());
        assertEquals(300L, saved.getUpdatedAt());
        assertEquals(200L, saved.getReadUpdatedAt());
        assertEquals(author, saved.getAuthorId());
        books.save(saved);
        assertEquals(saved, repository(2000).getById(book.getId()));
        saved.setName("Новое название");
        books.save(saved);
        assertEquals(2000L, books.getById(book.getId()).getUpdatedAt());
        assertEquals(100L, books.getById(book.getId()).getCreatedAt());
        assertEquals(200L, books.getById(book.getId()).getReadUpdatedAt());
    }

    @Test
    void requiredDatesCannotBeClearedButReadDateCan() {
        BookRepository books = repository(1000);
        Book book = Book.builder().name("Книга").read(true).authorId(UUID.randomUUID()).publisherId(UUID.randomUUID()).build();
        books.save(book);
        Book original = books.getById(book.getId());
        Book edited = books.getById(book.getId());
        edited.setCreatedAt(null);
        BookPropertiesService service = new BookPropertiesService(books);
        assertThrows(IllegalArgumentException.class, () -> service.save(original, edited, Set.of("createdAt")));
        assertEquals(original, books.getById(book.getId()));
        edited.setCreatedAt(original.getCreatedAt());
        edited.setReadUpdatedAt(null);
        assertNull(service.save(original, edited, Set.of("readUpdatedAt")).getReadUpdatedAt());
    }

    @Test
    void staleEditorCannotOverwriteNewerChanges() {
        BookRepository books = repository(1000);
        Book book = Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID()).build();
        books.save(book);
        Book original = books.getById(book.getId());
        Book edited = books.getById(book.getId());
        book.setName("Изменена в другом окне");
        books.save(book);
        assertThrows(IllegalStateException.class, () -> new BookPropertiesService(books).save(original, edited, Set.of()));
        assertEquals("Изменена в другом окне", books.getById(book.getId()).getName());
    }

    @Test
    void explicitDateEditsAreRolledBackWithTheTransaction() {
        BookRepository books = repository(1000);
        Book book = Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID()).build();
        books.save(book);
        Book original = books.getById(book.getId());
        books.beginTransaction();
        Book edited = books.getById(book.getId());
        edited.setCreatedAt(50L);
        edited.setUpdatedAt(60L);
        books.saveWithDates(edited, Set.of("createdAt", "updatedAt"));
        books.abortTransaction();
        assertEquals(original, books.getById(book.getId()));
        assertEquals(original, repository(2000).getById(book.getId()));
    }

    private BookRepository repository(long millis) {
        return new BookRepository(directory, Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC));
    }
}
