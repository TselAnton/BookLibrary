package com.tsel.home.project.booklibrary.book;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookRepositoryTest {

    private static final String TEST_STORAGE_FILE = "test_storage_file.txt";
    private static final Path TEST_STORAGE_FILE_PATH = Paths.get(TEST_STORAGE_FILE);

    @AfterEach
    public void clearFile() throws IOException {
        Files.delete(TEST_STORAGE_FILE_PATH);
    }

    @Test
    public void createRepositoryShouldCreateFileIfItNotExist() {
        new BookRepository(TEST_STORAGE_FILE);

        assertTrue(Files.exists(TEST_STORAGE_FILE_PATH));
    }

    @Test
    public void alreadyCreatedStorageFileShouldUsed() throws IOException {
        Map<Long, Book> books = new HashMap<>();
        books.put(1L, createBook(1L));
        books.put(2L, createBook(2L));
        books.put(3L, createBook(3L));
        books.put(4L, createBook(4L));

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(TEST_STORAGE_FILE))) {
            objectOutputStream.writeObject(books);
            objectOutputStream.flush();
        }

        BookRepository repository = new BookRepository(TEST_STORAGE_FILE);

        assertThat(repository.getAll(), containsInAnyOrder(books.values().toArray()));
    }

    private Book createBook(long number) {
        return BookBuilder.buildBook()
                .name("name" + number)
                .publisher("publisher" + number)
                .author("author" + number)
                .pages(100)
                .bookshelf(1)
                .read(true)
                .fullSeries(true)
                .build();
    }
}
