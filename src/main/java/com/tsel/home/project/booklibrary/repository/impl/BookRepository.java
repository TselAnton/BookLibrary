package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;

public class BookRepository extends AbstractFileRepository<Book> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-books-storage.txt";

    private static BookRepository INSTANCE;

    public static BookRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BookRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected BookRepository(String storageFileName) {
        super(storageFileName);
    }
}
