package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@FileStorageName("bookStorage.json")
public class BookRepositoryV2 extends AbstractFileRepositoryV2<UUID, Book> {

    public static final BookRepositoryV2 INSTANCE = new BookRepositoryV2();

    private static final AuthorRepositoryV2 AUTHOR_REPOSITORY = AuthorRepositoryV2.INSTANCE;
    private static final PublisherRepositoryV2 PUBLISHER_REPOSITORY = PublisherRepositoryV2.INSTANCE;
    private static final CycleRepositoryV2 CYCLE_REPOSITORY = CycleRepositoryV2.INSTANCE;

    protected BookRepositoryV2() {
        super(Book.class, new UUIDIdentifierGenerator());
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
        return this.repositoryMap.values()
            .stream()
            .noneMatch(checkPredicate);
    }

    @Override
    protected void compareEntities(Book newEntity, Book oldEntity) throws ConstraintException {
        if (isSameBook(oldEntity, newEntity)) {
            throw new ConstraintException(
                this.entityDisplayName,
                "книга с таким же именем, автором, публицистом, циклом и номером серии уже существует"
            );
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
