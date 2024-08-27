package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.util.Objects;
import java.util.UUID;

@FileStorageName("bookCycleStorage.json")
public class CycleRepositoryV2 extends AbstractFileRepositoryV2<UUID, Cycle> {

    public static final CycleRepositoryV2 INSTANCE = new CycleRepositoryV2();

    protected CycleRepositoryV2() {
        super(Cycle.class, new UUIDIdentifierGenerator());
    }

    @Override
    protected void compareEntities(Cycle newEntity, Cycle oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("цикл с таким же названием уже существует");
        }
    }
}
