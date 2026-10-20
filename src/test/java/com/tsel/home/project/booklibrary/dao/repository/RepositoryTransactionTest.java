package com.tsel.home.project.booklibrary.dao.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositoryTransactionTest {

    @TempDir
    Path directory;

    @Test
    void rollbackRestoresNestedObjectsAndDoesNotWrite() throws Exception {
        BookRepository books = new BookRepository(directory);
        Book book = book();
        books.save(book);
        Path file = directory.resolve("bookStorage.json");
        byte[] original = Files.readAllBytes(file);
        books.beginTransaction();
        Book edited = books.getById(book.getId());
        edited.getAudioBookSiteIds().clear();
        books.save(edited);
        books.abortTransaction();

        assertEquals(1, books.getById(book.getId()).getAudioBookSiteIds().size());
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    void readsAndWritesAreDetached() {
        BookRepository books = new BookRepository(directory);
        Book book = book();
        books.save(book);
        book.getAudioBookSiteIds().clear();
        books.getById(book.getId()).getAudioBookSiteIds().clear();
        books.getAll().get(0).setName("Изменено без save");

        assertEquals(1, books.getById(book.getId()).getAudioBookSiteIds().size());
        assertEquals("Книга", books.getById(book.getId()).getName());
    }

    @Test
    void validationFailureRollsBackAllRepositoriesIncludingGenre() {
        AuthorRepository authors = new AuthorRepository(directory);
        GenreRepository genres = new GenreRepository(directory);
        assertThrows(RuntimeException.class, () -> RepositoryTransaction.execute(() -> {
            authors.save(new Author(null, "Автор"));
            genres.save(new Genre(null, "Жанр"));
            authors.save(new Author(null, null));
            return null;
        }, authors, genres));

        assertTrue(authors.getAll().isEmpty());
        assertTrue(genres.getAll().isEmpty());
        assertFalse(Files.exists(directory.resolve("authorStorage.json")));
        assertFalse(Files.exists(directory.resolve("bookGenreStorage.json")));
    }

    @Test
    void partialWriteFailureRestoresAllFilesAndMemory() throws Exception {
        AuthorRepository authors = new AuthorRepository(directory);
        GenreRepository genres = new GenreRepository(directory);
        Author author = new Author(null, "До");
        authors.save(author);
        Path authorFile = directory.resolve("authorStorage.json");
        byte[] original = Files.readAllBytes(authorFile);
        StorageWriteBatch batch = new StorageWriteBatch() {
            private int writes;

            @Override
            protected void write(Path path, byte[] content) throws IOException {
                super.write(path, content);
                if (++writes == 2) {
                    throw new IOException("Ошибка после усечения второго файла");
                }
            }
        };
        assertThrows(IllegalStateException.class, () -> RepositoryTransaction.execute(batch, () -> {
            author.setName("После");
            authors.save(author);
            genres.save(new Genre(null, "Жанр"));
            return null;
        }, authors, genres));

        assertEquals("До", authors.getById(author.getId()).getName());
        assertTrue(genres.getAll().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(authorFile));
        assertFalse(Files.exists(directory.resolve("bookGenreStorage.json")));
    }

    @Test
    void noChangesDoNotWriteFiles() {
        AuthorRepository authors = new AuthorRepository(directory);
        Author author = new Author(null, "Автор");
        authors.save(author);
        StorageWriteBatch batch = new StorageWriteBatch() {
            @Override
            protected void write(Path path, byte[] content) {
                fail("Неизменённый файл не должен записываться");
            }
        };
        RepositoryTransaction.execute(batch, () -> authors.save(author), authors);
    }

    @Test
    void nestedTransactionDoesNotAbortExistingTransaction() {
        AuthorRepository authors = new AuthorRepository(directory);
        authors.beginTransaction();
        Author author = new Author(null, "Автор");
        authors.save(author);
        assertThrows(IllegalStateException.class, () -> RepositoryTransaction.execute(() -> null, authors));
        assertNotNull(authors.getById(author.getId()));
        authors.abortTransaction();
        assertTrue(authors.getAll().isEmpty());
    }

    private Book book() {
        return Book.builder().name("Книга").authorId(UUID.randomUUID()).publisherId(UUID.randomUUID())
            .publicationYear(2020).read(false).audioBookSiteIds(new ArrayList<>(List.of(UUID.randomUUID()))).build();
    }
}
