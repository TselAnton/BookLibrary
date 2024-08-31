package com.tsel.home.project.booklibrary.dao.repository.impl;

import static com.tsel.home.project.booklibrary.provider.SimpleApplicationContextProvider.getBean;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@FileStorageName("bookStorage.json")
public class BookRepositoryV2 extends AbstractFileRepositoryV2<UUID, Book> {

    private final AuthorRepositoryV2 authorRepository = getBean(AuthorRepositoryV2.class);
    private final PublisherRepositoryV2 publisherRepository = getBean(PublisherRepositoryV2.class);
    private final CycleRepositoryV2 cycleRepository = getBean(CycleRepositoryV2.class);

    public BookRepositoryV2(Path rootPath) {
        super(Book.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public BookRepositoryV2() {
        super(Book.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    @Override
    public void delete(Book entity) {
        super.delete(entity);

        if (isLatestValue(book -> Objects.equals(book.getAuthorId(), entity.getAuthorId()))) {
            authorRepository.deleteById(entity.getAuthorId());
        }
        if (isLatestValue(book -> Objects.equals(book.getPublisherId(), entity.getPublisherId()))) {
            publisherRepository.deleteById(entity.getPublisherId());
        }
        if (isLatestValue(book -> Objects.equals(book.getCycleId(), entity.getCycleId()))) {
            cycleRepository.deleteById(entity.getCycleId());
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
