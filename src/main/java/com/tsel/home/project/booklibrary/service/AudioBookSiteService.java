package com.tsel.home.project.booklibrary.service;

import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.RepositoryTransaction;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import java.util.UUID;

public final class AudioBookSiteService {

    private final BookRepository books;
    private final AudioBookSiteRepository sites;

    public AudioBookSiteService(BookRepository books, AudioBookSiteRepository sites) {
        this.books = books;
        this.sites = sites;
    }

    public void delete(UUID id) {
        RepositoryTransaction.execute(() -> {
            for (Book book : books.getAll()) {
                if (book.getAudioBookSiteIds() != null && book.getAudioBookSiteIds().removeIf(id::equals)) {
                    books.save(book);
                }
            }
            sites.deleteById(id);
            return null;
        }, books, sites);
    }
}
