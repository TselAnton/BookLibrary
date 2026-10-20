package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.google.gson.JsonObject;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@FileStorageName("bookStorage.json")
public class BookRepository extends AbstractFileRepository<UUID, Book> {

    private final Clock clock;

    public BookRepository(Path rootPath) {
        this(rootPath, Clock.systemUTC());
    }

    public BookRepository(Path rootPath, Clock clock) {
        super(Book.class, new UUIDIdentifierGenerator(), rootPath);
        this.clock = clock;
    }

    public BookRepository() {
        this(DEFAULT_REPOSITORY_PATH);
    }

    @Override
    protected void compareEntities(Book newEntity, Book oldEntity) throws ConstraintException {
        if (isSameBook(oldEntity, newEntity)) {
            throw buildConstraintException("книга с таким же именем, автором, публицистом, циклом и номером серии уже существует");
        }
    }

    private boolean isSameBook(Book existedBook, Book newBook) {
        return Objects.equals(existedBook.getName(), newBook.getName())
            && Objects.equals(existedBook.getAuthorId(), newBook.getAuthorId())
            && Objects.equals(existedBook.getPublisherId(), newBook.getPublisherId())
            && Objects.equals(existedBook.getCycleId(), newBook.getCycleId())
            && Objects.equals(existedBook.getNumberInSeries(), newBook.getNumberInSeries())
            && Objects.equals(existedBook.getPublicationYear(), newBook.getPublicationYear());
    }

    @Override
    protected Clock storageClock() {
        return clock;
    }

    @Override
    protected void prepareSave(Book entity, Book previous) {
        long now = clock.millis();
        if (previous == null) {
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setReadUpdatedAt(Boolean.TRUE.equals(entity.getRead()) ? now : null);
            return;
        }
        if (previous.getCreatedAt() != null) {
            entity.setCreatedAt(previous.getCreatedAt());
        }
        if (previous.getUpdatedAt() != null) {
            entity.setUpdatedAt(previous.getUpdatedAt());
        }
        entity.setReadUpdatedAt(previous.getReadUpdatedAt());
        if (!businessState(entity).equals(businessState(previous))) {
            entity.setUpdatedAt(now);
        }
        if (!Objects.equals(entity.getRead(), previous.getRead())) {
            entity.setReadUpdatedAt(now);
        }
    }

    private JsonObject businessState(Book book) {
        JsonObject state = GSON.toJsonTree(book).getAsJsonObject();
        state.remove("createdAt");
        state.remove("updatedAt");
        state.remove("readUpdatedAt");
        return state;
    }
}
