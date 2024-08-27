package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

@FileStorageName("publisherStorage.json")
public class PublisherRepositoryV2 extends AbstractFileRepositoryV2<UUID, Publisher> {

    private static PublisherRepositoryV2 instance;

    public static PublisherRepositoryV2 getInstance(Path... paths) {
        if (instance == null) {
            instance = new PublisherRepositoryV2(Arrays.stream(paths).findFirst().orElse(null));
        }
        return instance;
    }

    public PublisherRepositoryV2(@Nullable Path rootPath) {
        super(Publisher.class, new UUIDIdentifierGenerator(), rootPath);
    }

    @Override
    protected void compareEntities(Publisher newEntity, Publisher oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("публицист с таким же названием уже существует");
        }
    }
}
