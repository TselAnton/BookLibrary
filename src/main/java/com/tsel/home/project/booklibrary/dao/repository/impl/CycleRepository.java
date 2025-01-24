package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@FileStorageName("bookCycleStorage.json")
public class CycleRepository extends AbstractFileRepository<UUID, Cycle> {

    public CycleRepository(Path rootPath) {
        super(Cycle.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public CycleRepository() {
        super(Cycle.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    public Optional<Cycle> getByName(String name) {
        return getRepositoryMap().values()
            .stream()
            .filter(cycle -> cycle.getName().contains(name))
            .findFirst();
    }

    @Override
    protected void compareEntities(Cycle newEntity, Cycle oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("цикл с таким названием уже существует");
        }
    }
}
