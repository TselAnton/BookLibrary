package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;

public class AuthorRepository extends AbstractFileRepository<Author> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-authors-storage.txt";

    private static AuthorRepository INSTANCE;

    public static AuthorRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthorRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected AuthorRepository(String storageFileName) {
        super(storageFileName);
    }
}
