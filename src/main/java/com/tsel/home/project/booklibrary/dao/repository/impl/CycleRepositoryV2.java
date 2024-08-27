package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

@FileStorageName("bookCycleStorage.json")
public class CycleRepositoryV2 extends AbstractFileRepositoryV2<UUID, Cycle> {

    private static CycleRepositoryV2 instance;

    public static CycleRepositoryV2 getInstance(Path... paths) {
        if (instance == null) {
            instance = new CycleRepositoryV2(Arrays.stream(paths).findFirst().orElse(null));
        }
        return instance;
    }

    public CycleRepositoryV2(@Nullable Path rootPath) {
        super(Cycle.class, new UUIDIdentifierGenerator(), rootPath);
    }

    @Override
    protected void compareEntities(Cycle newEntity, Cycle oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("цикл с таким же названием уже существует");
        }
    }
}
