package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.dao.data.Author;

@Deprecated(since = "4.0")
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
