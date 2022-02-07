package com.tsel.home.project.booklibrary.book;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class BookRepository {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my_library_books_storage.txt";

    private static BookRepository INSTANCE;

    private final String storageFileName;
    private final LinkedHashMap<String, Book> bookMap;

    public static BookRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BookRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected BookRepository(String storageFileName) {
        this.storageFileName = storageFileName;

        if (isStorageAlreadyExist()) {
            try {
                bookMap = readStorageFile();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(format("Problem while reading file: %s", storageFileName), e);
            }

        } else {
            try {
                createNewStorageFile();
                bookMap = new LinkedHashMap<>();
            } catch (IOException e) {
                throw new IllegalStateException(format("Problem while creating file: %s", storageFileName), e);
            }
        }
    }

    private boolean isStorageAlreadyExist() {
        Path path = Paths.get(storageFileName);
        return Files.exists(path);
    }

    private void createNewStorageFile() throws IOException {
        Path path = Paths.get(storageFileName);
        Files.createFile(path);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Book> readStorageFile() throws FileNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(storageFileName))) {
            return (LinkedHashMap<String, Book>) inputStream.readObject();

        } catch (EOFException e) {
            return new LinkedHashMap<>();
        } catch (IOException e) {
            throw new IllegalStateException(format("Problem while reading file: %s", storageFileName), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(format("Cant cast object from: %s", storageFileName), e);
        }
    }

    private void updateStorageFile() {
        try (ObjectOutputStream inputStream = new ObjectOutputStream(new FileOutputStream(storageFileName))) {
            inputStream.writeObject(bookMap);
            inputStream.flush();

        } catch (IOException e) {
            throw new IllegalStateException(format("Problem while writing in the file: %s", storageFileName), e);
        }
    }

    /**
     * Get all {@link Book}'s
     * @return {@link List} of {@link Book}'s
     */
    public List<Book> getAll() {
        return bookMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Save new {@link Book}
     * @param book {@link Book}
     * @return saved {@link Book}
     */
    public Book save(Book book) {
        bookMap.put(book.getName(), book);
        updateStorageFile();
        return book;
    }

    /**
     * Delete {@link Book} by ID
     * @param book {@link Book} ID
     */
    public void delete(Book book) {
        bookMap.remove(book.getName());
        updateStorageFile();
    }

    /**
     * Get {@link Book} by Name
     * @param name {@link Book} name
     * @return {@link Book}
     */
    public Book getBookByName(String name) {
        return bookMap.get(name);
    }
}
