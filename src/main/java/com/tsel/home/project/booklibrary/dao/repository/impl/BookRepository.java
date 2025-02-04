package com.tsel.home.project.booklibrary.dao.repository.impl;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@FileStorageName("bookStorage.json")
public class BookRepository extends AbstractFileRepository<UUID, Book> {

    public BookRepository(Path rootPath) {
        super(Book.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public BookRepository() {
        super(Book.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    @Override
    public void delete(Book entity) {
        super.delete(entity);

        if (isLatestValue(book -> Objects.equals(book.getAuthorId(), entity.getAuthorId()))) {
            getBean(AuthorRepository.class).deleteById(entity.getAuthorId());
        }
        if (isLatestValue(book -> Objects.equals(book.getPublisherId(), entity.getPublisherId()))) {
            getBean(PublisherRepository.class).deleteById(entity.getPublisherId());
        }
        if (isLatestValue(book -> Objects.equals(book.getCycleId(), entity.getCycleId()))) {
            getBean(CycleRepository.class).deleteById(entity.getCycleId());
        }
        if (isLatestValue(book -> Objects.equals(book.getGenreId(), entity.getGenreId()))) {
            getBean(GenreRepository.class).deleteById(entity.getGenreId());
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
            && Objects.equals(existedBook.getNumberInSeries(), newBook.getNumberInSeries())
            && Objects.equals(existedBook.getPublicationYear(), newBook.getPublicationYear());
    }

    @Override
    protected void preCreateEntity(Book entity) {
        if (entity.getPublicationYear() == null) {
            entity.setPublicationYear(0);   // Задаётся дефолтный год, если он не был указан
        }
    }
}
