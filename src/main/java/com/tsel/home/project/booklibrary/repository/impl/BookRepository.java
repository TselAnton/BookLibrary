package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.utils.StringUtils;

import java.util.function.Function;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

public class BookRepository extends AbstractFileRepository<Book> {

    private static final AuthorRepository AUTHOR_REPOSITORY = AuthorRepository.getInstance();
    private static final PublisherRepository PUBLISHER_REPOSITORY = PublisherRepository.getInstance();
    private static final CycleRepository CYCLE_REPOSITORY = CycleRepository.getInstance();

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-books-storage.txt";

    private static BookRepository INSTANCE;

    public static BookRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BookRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected BookRepository(String storageFileName) {
        super(storageFileName);
    }

    @Override
    public void delete(Book entity) {
        String author = entity.getAuthor();
        String publisher = entity.getPublisher();
        String cycle = entity.getCycleName();

        super.delete(entity);

        if (isNotLinkedValue(author, Book::getAuthor)) {
            AUTHOR_REPOSITORY.delete(new Author(author));
        }

        if (isNotLinkedValue(publisher, Book::getPublisher)) {
            PUBLISHER_REPOSITORY.delete(new Publisher(publisher));
        }

        if (isNotBlank(cycle) && isNotLinkedValue(cycle, Book::getCycleName)) {
            Cycle oldCycle = CYCLE_REPOSITORY.getByName(cycle);
            CYCLE_REPOSITORY.delete(oldCycle);
        }
    }

    private boolean isNotLinkedValue(String value, Function<Book, String> mapFunc) {
        return repositoryMap.values()
                .stream()
                .map(mapFunc)
                .filter(StringUtils::isNotBlank)
                .noneMatch(field -> field.equals(value));
    }

    @Override
    protected void updateNewFields() {
        repositoryMap.values()
            .forEach(book -> {
                    prepareAutograph(book);
                    preparePrice(book);
                    prepareHardCover(book);
                }
            );

        updateStorageFile();
    }

    private void prepareAutograph(Book book) {
        book.setAutograph(
            book.getAutograph() != null
                ? book.getAutograph()
                : false
        );
    }

    private void preparePrice(Book book) {
        book.setPrice(book.getPrice() == null ? null : book.getPrice());
    }

    private void prepareHardCover(Book book) {
        book.setHardCover(book.getHardCover() == null || book.getHardCover());
    }
}
