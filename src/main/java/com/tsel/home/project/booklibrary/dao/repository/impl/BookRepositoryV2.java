package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;

@FileStorageName("bookStorage.json")
public class BookRepositoryV2 extends AbstractFileRepositoryV2<UUID, Book> {

    private static final AuthorRepositoryV2 AUTHOR_REPOSITORY = AuthorRepositoryV2.getInstance();
    private static final PublisherRepositoryV2 PUBLISHER_REPOSITORY = PublisherRepositoryV2.getInstance();
    private static final CycleRepositoryV2 CYCLE_REPOSITORY = CycleRepositoryV2.getInstance();

    private static BookRepositoryV2 instance;

    public static BookRepositoryV2 getInstance(Path... paths) {
        if (instance == null) {
            instance = new BookRepositoryV2(Arrays.stream(paths).findFirst().orElse(null));
        }
        return instance;
    }

    public BookRepositoryV2(@Nullable Path rootPath) {
        super(Book.class, new UUIDIdentifierGenerator(), rootPath);
    }

    @Override
    public void delete(Book entity) {
        super.delete(entity);

        if (isLatestValue(book -> Objects.equals(book.getAuthorId(), entity.getAuthorId()))) {
            AUTHOR_REPOSITORY.deleteById(entity.getAuthorId());
        }
        if (isLatestValue(book -> Objects.equals(book.getPublisherId(), entity.getPublisherId()))) {
            PUBLISHER_REPOSITORY.deleteById(entity.getPublisherId());
        }
        if (isLatestValue(book -> Objects.equals(book.getCycleId(), entity.getCycleId()))) {
            CYCLE_REPOSITORY.deleteById(entity.getCycleId());
        }
    }

    private boolean isLatestValue(Predicate<Book> checkPredicate) {
        return this.getRepositoryMap().values()
            .stream()
            .noneMatch(checkPredicate);
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
            && Objects.equals(existedBook.getNumberInSeries(), newBook.getNumberInSeries());
    }
}
