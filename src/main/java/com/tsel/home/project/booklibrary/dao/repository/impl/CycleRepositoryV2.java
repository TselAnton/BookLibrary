package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.util.UUID;

@FileStorageName("bookCycleStorage.json")
public class CycleRepositoryV2 extends AbstractFileRepositoryV2<UUID, Cycle> {

    public static final CycleRepositoryV2 INSTANCE = new CycleRepositoryV2();

    protected CycleRepositoryV2() {
        super(Cycle.class, new UUIDIdentifierGenerator());
    }
}
