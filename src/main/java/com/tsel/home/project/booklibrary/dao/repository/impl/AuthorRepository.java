package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@FileStorageName("authorStorage.json")
public class AuthorRepository extends AbstractFileRepository<UUID, Author> {

    public AuthorRepository(Path rootPath) {
        super(Author.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public AuthorRepository() {
        super(Author.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    public Optional<Author> getByName(String name) {
        return getRepositoryMap().values()
            .stream()
            .filter(author -> author.getName().contains(name))
            .findFirst();
    }

    @Override
    protected void compareEntities(Author newEntity, Author oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("автор с таким же именем уже существует");
        }
    }
}
