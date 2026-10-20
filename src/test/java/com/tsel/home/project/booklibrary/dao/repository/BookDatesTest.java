package com.tsel.home.project.booklibrary.dao.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookDatesTest {

    @TempDir
    Path directory;

    @Test
    void datesTrackBothReadTransitionsAndRealChanges() throws Exception {
        BookRepository books = repository(1000);
        Book book = Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID())
            .read(false).build();
        books.save(book);
        book = books.getById(book.getId());
        assertEquals(1000L, book.getCreatedAt());
        assertEquals(1000L, book.getUpdatedAt());
        assertNull(book.getReadUpdatedAt());

        books = repository(2000);
        book.setRead(true);
        books.save(book);
        book = books.getById(book.getId());
        assertEquals(1000L, book.getCreatedAt());
        assertEquals(2000L, book.getReadUpdatedAt());
        assertEquals(2000L, book.getUpdatedAt());

        books = repository(3000);
        book.setRead(false);
        books.save(book);
        book = books.getById(book.getId());
        assertEquals(3000L, book.getReadUpdatedAt());
        assertEquals(3000L, book.getUpdatedAt());

        books = repository(4000);
        book.setCoverImgAbsolutePath("cover.png");
        books.save(book);
        book = books.getById(book.getId());
        assertEquals(4000L, book.getUpdatedAt());
        assertEquals(3000L, book.getReadUpdatedAt());

        byte[] original = Files.readAllBytes(directory.resolve("bookStorage.json"));
        books = repository(5000);
        book.setCreatedAt(99L);
        book.setUpdatedAt(99L);
        book.setReadUpdatedAt(null);
        books.save(book);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("bookStorage.json")));
    }

    @Test
    void newReadBookGetsOneTimestamp() {
        BookRepository books = repository(1234);
        Book book = Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID())
            .read(true).build();
        books.save(book);
        Book saved = books.getById(book.getId());
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());
        assertEquals(saved.getCreatedAt(), saved.getReadUpdatedAt());
    }

    @Test
    void failedCommitRestoresAllDatesAndDisk() throws Exception {
        BookRepository originalRepository = repository(1000);
        Book book = Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID())
            .read(false).build();
        originalRepository.save(book);
        Book original = originalRepository.getById(book.getId());
        Path file = directory.resolve("bookStorage.json");
        byte[] bytes = Files.readAllBytes(file);
        BookRepository books = repository(2000);
        StorageWriteBatch batch = new StorageWriteBatch() {
            private boolean failed;

            @Override
            protected void write(Path path, byte[] content) throws IOException {
                super.write(path, content);
                if (!failed) {
                    failed = true;
                    throw new IOException("Ошибка после записи");
                }
            }
        };
        assertThrows(IllegalStateException.class, () -> RepositoryTransaction.execute(batch, () -> {
            Book edited = books.getById(book.getId());
            edited.setRead(true);
            books.save(edited);
            assertEquals(2000L, books.getById(book.getId()).getReadUpdatedAt());
            return null;
        }, books));
        assertEquals(original, books.getById(book.getId()));
        assertArrayEquals(bytes, Files.readAllBytes(file));
        assertEquals(original, repository(3000).getById(book.getId()));
    }

    @Test
    void legacyDatesUseTheRepositoryClockThroughSchemaDefaults() throws Exception {
        Book book = Book.builder().id(UUID.randomUUID()).name("Книга").authorId(UUID.randomUUID())
            .publisherId(UUID.randomUUID()).read(false).publicationYear(0)
            .audioBookSiteIds(new java.util.ArrayList<>()).build();
        Files.writeString(directory.resolve("bookStorage.json"),
            com.tsel.home.project.booklibrary.utils.MyGson.buildGson().toJson(java.util.List.of(book)));
        BookRepository books = repository(1234);
        books.save(books.getById(book.getId()));
        assertEquals(1234L, books.getById(book.getId()).getCreatedAt());
        assertEquals(1234L, books.getById(book.getId()).getUpdatedAt());
    }

    private BookRepository repository(long millis) {
        return new BookRepository(directory, Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC));
    }
}
