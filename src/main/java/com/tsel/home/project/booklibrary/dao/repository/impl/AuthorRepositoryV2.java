package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.IdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

@FileStorageName("authorStorage.json")
public class AuthorRepositoryV2 extends AbstractFileRepositoryV2<UUID, Author> {

    public static final AuthorRepositoryV2 INSTANCE = new AuthorRepositoryV2(Author.class, new UUIDIdentifierGenerator(), null);

    public AuthorRepositoryV2(Class<Author> entityClass, IdentifierGenerator<UUID> keyGenerator, @Nullable Path rootPath) {
        super(entityClass, keyGenerator, rootPath);
    }

    @Override
    protected void compareEntities(Author newEntity, Author oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("автор с таким же именем уже существует");
        }
    }
}
