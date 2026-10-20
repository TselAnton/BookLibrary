package com.tsel.home.project.booklibrary.service;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.RepositoryTransaction;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;

/** Согласованное сохранение книги и справочников из редактора. */
public final class BookSaveService {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final PublisherRepository publishers;
    private final GenreRepository genres;
    private final CycleRepository cycles;

    public BookSaveService(
        BookRepository books,
        AuthorRepository authors,
        PublisherRepository publishers,
        GenreRepository genres,
        CycleRepository cycles
    ) {
        this.books = books;
        this.authors = authors;
        this.publishers = publishers;
        this.genres = genres;
        this.cycles = cycles;
    }

    public Book save(Book book, Author author, Publisher publisher, Genre genre, Cycle cycle) {
        return RepositoryTransaction.execute(() -> {
            book.setAuthorId(authors.save(author));
            book.setPublisherId(publishers.save(publisher));
            book.setGenreId(genre == null ? null : genres.save(genre));
            book.setCycleId(cycle == null ? null : cycles.save(cycle));
            books.save(book);
            return books.getById(book.getId());
        }, authors, publishers, genres, cycles, books);
    }
}
