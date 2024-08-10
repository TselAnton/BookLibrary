package com.tsel.home.project.booklibrary.repository.impl;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.data.Cycle;
import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import java.util.function.Function;

@FileStorageName("bookStorage.json")
public class BookRepositoryV2 extends AbstractFileRepositoryV2<String, Book> {

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
}
