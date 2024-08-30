package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@FileStorageName("authorStorage.json")
public class AuthorRepositoryV2 extends AbstractFileRepositoryV2<UUID, Author> {

    public AuthorRepositoryV2(Path rootPath) {
        super(Author.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public AuthorRepositoryV2() {
        super(Author.class, new UUIDIdentifierGenerator(), null);
    }

    @Override
    protected void compareEntities(Author newEntity, Author oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("автор с таким же именем уже существует");
        }
    }
}
