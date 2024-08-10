package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;

@Deprecated
public class PublisherRepository extends AbstractFileRepository<Publisher> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-publishers-storage.txt";

    private static PublisherRepository INSTANCE;

    public static PublisherRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PublisherRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected PublisherRepository(String storageFileName) {
        super(storageFileName);
    }
}
