package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@FileStorageName("bookGenreStorage.json")
public class GenreRepository extends AbstractFileRepository<UUID, Genre> {

    public GenreRepository(Path rootPath) {
        super(Genre.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public GenreRepository() {
        super(Genre.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    public Optional<Genre> getByName(String name) {
        return getRepositoryMap().values()
            .stream()
            .filter(genre -> genre.getName().contains(name))
            .findFirst();
    }

    @Override
    protected void compareEntities(Genre newEntity, Genre oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("жанр с таким названием уже существует");
        }
    }
}
