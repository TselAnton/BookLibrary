package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("bookCycleStorage.json")
public class CycleRepositoryV2 extends AbstractFileRepositoryV2<String, Cycle> {

    private static CycleRepositoryV2 INSTANCE;

    public static CycleRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CycleRepositoryV2();
        }
        return INSTANCE;
    }

    protected CycleRepositoryV2() {
        super(Cycle.class);
    }
}
