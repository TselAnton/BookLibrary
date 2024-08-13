package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.util.UUID;

@FileStorageName("bookCycleStorage.json")
public class CycleRepositoryV2 extends AbstractFileRepositoryV2<UUID, Cycle> {

    private static CycleRepositoryV2 INSTANCE;

    public static CycleRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CycleRepositoryV2();
        }
        return INSTANCE;
    }

    protected CycleRepositoryV2() {
        super(Cycle.class, UUIDIdentifierGenerator.INSTANCE);
    }
}
