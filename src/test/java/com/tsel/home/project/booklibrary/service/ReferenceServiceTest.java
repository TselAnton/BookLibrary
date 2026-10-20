package com.tsel.home.project.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReferenceServiceTest {

    @TempDir
    Path directory;

    private BookRepository books;
    private AuthorRepository authors;
    private CycleRepository cycles;
    private GenreRepository genres;
    private PublisherRepository publishers;
    private ReferenceService service;

    @BeforeEach
    void initialize() {
        books = new BookRepository(directory);
        authors = new AuthorRepository(directory);
        cycles = new CycleRepository(directory);
        genres = new GenreRepository(directory);
        publishers = new PublisherRepository(directory);
        service = new ReferenceService(books, authors, cycles, genres, publishers);
    }

    @Test
    void allReferenceTypesKeepLinksAndCannotBeDeletedWhileUsed() {
        UUID author = authors.save(new Author(null, "Автор"));
        UUID cycle = cycles.save(new Cycle(null, "Цикл", false, 3));
        UUID genre = genres.save(new Genre(null, "Жанр"));
        UUID publisher = publishers.save(new Publisher(null, "Публицист"));
        Book book = Book.builder().name("Книга").authorId(author).publisherId(publisher)
            .genreId(genre).cycleId(cycle).publicationYear(2026).build();
        books.save(book);

        for (ReferenceType type : ReferenceType.values()) {
            UUID id = type.referenceId(book);
            assertEquals(1, service.entries(type).get(0).bookCount());
            assertEquals(book.getId(), service.linkedBooks(type, id).get(0).getId());
            assertThrows(IllegalStateException.class, () -> service.delete(type, id));
            service.save(type, id, " Новое название ", true, 0);
            assertEquals("Новое название", service.entries(type).get(0).name());
            assertEquals(id, type.referenceId(new BookRepository(directory).getById(book.getId())));
        }
        assertTrue(new CycleRepository(directory).getById(cycle).getEnded());
        assertEquals(0, new CycleRepository(directory).getById(cycle).getBooksInCycle());
        assertEquals("Новое название", new AuthorRepository(directory).getById(author).getName());
        assertEquals("Новое название", new GenreRepository(directory).getById(genre).getName());
        assertEquals("Новое название", new PublisherRepository(directory).getById(publisher).getName());

        books.delete(book);
        for (ReferenceType type : ReferenceType.values()) {
            assertEquals(0, service.entries(type).get(0).bookCount());
            service.delete(type, type.referenceId(book));
            assertTrue(service.entries(type).isEmpty());
        }
        assertTrue(new AuthorRepository(directory).getAll().isEmpty());
        assertTrue(new CycleRepository(directory).getAll().isEmpty());
        assertTrue(new GenreRepository(directory).getAll().isEmpty());
        assertTrue(new PublisherRepository(directory).getAll().isEmpty());
    }

    @Test
    void invalidEditsPreserveOriginalRecordOnDiskAndInMemory() {
        UUID first = authors.save(new Author(null, "Семён"));
        UUID second = authors.save(new Author(null, "Другой"));
        assertThrows(IllegalArgumentException.class, () -> service.save(ReferenceType.AUTHORS, second, "  СЕМЕН ", null, null));
        assertThrows(IllegalArgumentException.class, () -> service.save(ReferenceType.AUTHORS, first, " ", null, null));
        assertEquals("Другой", authors.getById(second).getName());
        assertEquals("Другой", new AuthorRepository(directory).getById(second).getName());
        UUID cycle = cycles.save(new Cycle(null, "Цикл", false, 3));
        assertThrows(IllegalArgumentException.class, () -> service.save(ReferenceType.CYCLES, cycle, "Цикл", true, -1));
        assertFalse(cycles.getById(cycle).getEnded());
        assertEquals(3, new CycleRepository(directory).getById(cycle).getBooksInCycle());
    }

    @Test
    void failedCommitRestoresOriginalEntryAndAllowsRetry() {
        FailingAuthorRepository failing = new FailingAuthorRepository(directory);
        UUID id = failing.save(new Author(null, "Исходное имя"));
        ReferenceService transactional = new ReferenceService(books, failing, cycles, genres, publishers);
        failing.failCommit = true;
        assertThrows(IllegalStateException.class, () -> transactional.save(ReferenceType.AUTHORS, id, "Правка", null, null));
        assertEquals("Исходное имя", failing.getById(id).getName());
        assertEquals("Исходное имя", new AuthorRepository(directory).getById(id).getName());
        transactional.save(ReferenceType.AUTHORS, id, "Правка", null, null);
        assertEquals("Правка", new AuthorRepository(directory).getById(id).getName());

        failing.failCommit = true;
        assertThrows(IllegalStateException.class, () -> transactional.delete(ReferenceType.AUTHORS, id));
        assertTrue(failing.existById(id));
        assertTrue(new AuthorRepository(directory).existById(id));
    }

    @FileStorageName("authorStorage.json")
    private static class FailingAuthorRepository extends AuthorRepository {

        private boolean failCommit;

        private FailingAuthorRepository(Path directory) {
            super(directory);
        }

        @Override
        public void commitTransaction() {
            if (failCommit) {
                failCommit = false;
                throw new IllegalStateException("Тестовый сбой записи");
            }
            super.commitTransaction();
        }
    }
}
