package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("bookCycleStorage.json")
public class CycleRepository extends AbstractFileRepository<Cycle> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-cycles-storage.txt";

    private static CycleRepository INSTANCE;

    public static CycleRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CycleRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected CycleRepository(String storageFileName) {
        super(storageFileName);
    }
}
