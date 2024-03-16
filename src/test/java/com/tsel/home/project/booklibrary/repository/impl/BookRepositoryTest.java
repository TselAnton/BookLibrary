package com.tsel.home.project.booklibrary.repository.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.builder.BookBuilder;
import com.tsel.home.project.booklibrary.data.Book;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
        Map<Long, Book> books = new LinkedHashMap<>();
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
        return BookBuilder.builder()
            .name("name" + number)
            .author("author" + number)
            .publisher("publisher" + number)
            .pages(100)
            .read(true)
            .price(null)
            .autograph(false)
            .hardCover(true)
            .build();
    }
}
