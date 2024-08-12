package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.dao.data.Cycle;

@Deprecated(since = "4.0")
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
