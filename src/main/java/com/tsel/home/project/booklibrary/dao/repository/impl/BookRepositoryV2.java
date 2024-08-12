package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.exception.RepositoryConstraintException;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.util.Objects;
import java.util.UUID;

@FileStorageName("bookStorage.json")
public class BookRepositoryV2 extends AbstractFileRepositoryV2<UUID, Book> {

    private static final AuthorRepositoryV2 AUTHOR_REPOSITORY = AuthorRepositoryV2.getInstance();
    private static final PublisherRepositoryV2 PUBLISHER_REPOSITORY = PublisherRepositoryV2.getInstance();
    private static final CycleRepositoryV2 CYCLE_REPOSITORY = CycleRepositoryV2.getInstance();

    private static BookRepositoryV2 INSTANCE;

    public static BookRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BookRepositoryV2();
        }
        return INSTANCE;
    }

    protected BookRepositoryV2() {
        super(Book.class);
    }

    @Override
    public void delete(Book entity) {
        super.delete(entity);

        if (isNotLinkedValue(book -> book.getAuthorId().equals(entity.getAuthorId()))) {
            AUTHOR_REPOSITORY.deleteById(entity.getAuthorId());
        }
        if (isNotLinkedValue(book -> book.getPublisherId().equals(entity.getPublisherId()))) {
            PUBLISHER_REPOSITORY.deleteById(entity.getPublisherId());
        }
        if (isNotLinkedValue(book -> book.getCycleId().equals(entity.getCycleId()))) {
            CYCLE_REPOSITORY.deleteById(entity.getCycleId());
        }
    }

    // TODO: переделать
    @Override
    protected void checkEntityConstrains(Book existedEntity, Book entityForSave) throws RepositoryConstraintException {
        if (existedEntity.getName() == null) {
            throw new RepositoryConstraintException(entityForSave, "книга должна иметь название");
        }
        if (existedEntity.getAuthorId() == null) {
            throw new RepositoryConstraintException(entityForSave, "книга должна иметь автора");
        }
        if (existedEntity.getPublisherId() == null) {
            throw new RepositoryConstraintException(entityForSave, "книга должна иметь публициста");
        }
        if (isSameBook(existedEntity, entityForSave)) {
            throw new RepositoryConstraintException(entityForSave, "книги не могут иметь одинаковое имя, автора, публициста, цикл и номер серии");
        }
    }

    private boolean isSameBook(Book existedBook, Book newBook) {
        return existedBook.getName().equalsIgnoreCase(newBook.getName())
            && existedBook.getAuthorId().equals(newBook.getAuthorId())
            && existedBook.getPublisherId().equals(newBook.getPublisherId())
            && Objects.equals(existedBook.getCycleId(), newBook.getCycleId())
            && Objects.equals(existedBook.getNumberInSeries(), newBook.getNumberInSeries());
    }
}
