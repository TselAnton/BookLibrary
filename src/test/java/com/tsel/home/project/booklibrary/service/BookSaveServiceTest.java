package com.tsel.home.project.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookSaveServiceTest {

    @TempDir
    Path directory;

    @Test
    void savesAllReferencesAndReturnsBookWithDates() {
        BookRepository books = books(1000);
        AuthorRepository authors = new AuthorRepository(directory);
        PublisherRepository publishers = new PublisherRepository(directory);
        GenreRepository genres = new GenreRepository(directory);
        CycleRepository cycles = new CycleRepository(directory);
        BookSaveService service = new BookSaveService(books, authors, publishers, genres, cycles);
        Book saved = service.save(Book.builder().name("Книга").build(), new Author(null, "Автор"),
            new Publisher(null, "Издатель"), new Genre(null, "Жанр"), new Cycle(null, "Цикл", null, null));

        assertNotNull(authors.getById(saved.getAuthorId()));
        assertNotNull(publishers.getById(saved.getPublisherId()));
        assertNotNull(genres.getById(saved.getGenreId()));
        assertEquals(1, cycles.getById(saved.getCycleId()).getBooksInCycle());
        assertEquals(1000L, saved.getCreatedAt());
        assertEquals(saved, new BookRepository(directory).getById(saved.getId()));
    }

    @Test
    void invalidBookDoesNotLeaveReferencesOnDisk() {
        BookRepository books = books(1000);
        AuthorRepository authors = new AuthorRepository(directory);
        PublisherRepository publishers = new PublisherRepository(directory);
        GenreRepository genres = new GenreRepository(directory);
        CycleRepository cycles = new CycleRepository(directory);
        BookSaveService service = new BookSaveService(books, authors, publishers, genres, cycles);
        assertThrows(RuntimeException.class, () -> service.save(new Book(), new Author(null, "Автор"),
            new Publisher(null, "Издатель"), new Genre(null, "Жанр"), null));
        assertTrue(authors.getAll().isEmpty());
        assertTrue(genres.getAll().isEmpty());
        assertFalse(Files.exists(directory.resolve("authorStorage.json")));
        assertFalse(Files.exists(directory.resolve("bookGenreStorage.json")));
    }

    @Test
    void deletingSiteUpdatesOnlyAffectedBooks() {
        BookRepository books = books(1000);
        AuthorRepository authors = new AuthorRepository(directory);
        PublisherRepository publishers = new PublisherRepository(directory);
        GenreRepository genres = new GenreRepository(directory);
        CycleRepository cycles = new CycleRepository(directory);
        BookSaveService service = new BookSaveService(books, authors, publishers, genres, cycles);
        AudioBookSiteRepository sites = new AudioBookSiteRepository(directory);
        AudioBookSite removed = new AudioBookSite(null, "Удаляемый");
        AudioBookSite retained = new AudioBookSite(null, "Остающийся");
        sites.save(removed);
        sites.save(retained);
        Author author = new Author(null, "Автор");
        Publisher publisher = new Publisher(null, "Издатель");
        Book affected = service.save(Book.builder().name("Первая")
            .audioBookSiteIds(new ArrayList<>(List.of(removed.getId(), retained.getId()))).build(), author, publisher, null, null);
        Book unchanged = service.save(Book.builder().name("Вторая")
            .audioBookSiteIds(new ArrayList<>(List.of(retained.getId()))).build(), author, publisher, null, null);
        books = books(2000);
        new AudioBookSiteService(books, sites).delete(removed.getId());

        assertFalse(sites.existById(removed.getId()));
        assertEquals(List.of(retained.getId()), books.getById(affected.getId()).getAudioBookSiteIds());
        assertEquals(2000L, books.getById(affected.getId()).getUpdatedAt());
        assertEquals(1000L, books.getById(unchanged.getId()).getUpdatedAt());
    }

    private BookRepository books(long time) {
        return new BookRepository(directory, Clock.fixed(Instant.ofEpochMilli(time), ZoneOffset.UTC));
    }
}
