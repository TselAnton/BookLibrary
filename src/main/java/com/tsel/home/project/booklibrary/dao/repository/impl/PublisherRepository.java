package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@FileStorageName("publisherStorage.json")
public class PublisherRepository extends AbstractFileRepository<UUID, Publisher> {

    public PublisherRepository(Path rootPath) {
        super(Publisher.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public PublisherRepository() {
        super(Publisher.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    public Optional<Publisher> getByName(String name) {
        return getRepositoryMap().values()
            .stream()
            .filter(publisher -> publisher.getName().contains(name))
            .findFirst();
    }

    @Override
    protected void compareEntities(Publisher newEntity, Publisher oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("публицист с таким же названием уже существует");
        }
    }
}
