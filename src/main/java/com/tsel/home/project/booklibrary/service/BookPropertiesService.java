package com.tsel.home.project.booklibrary.service;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.util.Objects;
import java.util.Set;

public final class BookPropertiesService {

    private static final Gson GSON = MyGson.buildGson();

    private final BookRepository books;

    public BookPropertiesService(BookRepository books) {
        this.books = books;
    }

    public Book save(Book original, Book edited, Set<String> dates) {
        Book current = books.getById(original.getId());
        if (current == null || !GSON.toJsonTree(current).equals(GSON.toJsonTree(original))) {
            throw new IllegalStateException("Книга изменена или удалена. Обновите список перед сохранением.");
        }
        if (!Objects.equals(original.getId(), edited.getId())) {
            throw new IllegalArgumentException("Идентификатор книги нельзя изменять");
        }
        if (edited.getName() == null || edited.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите название книги");
        }
        if (edited.getPages() != null && edited.getPages() < 0
            || edited.getPublicationYear() == null || edited.getPublicationYear() < 0
            || edited.getNumberInSeries() != null && edited.getNumberInSeries() < 0) {
            throw new IllegalArgumentException("Количество страниц, год и номер в цикле должны быть целыми числами от 0");
        }
        if (edited.getPrice() != null && (!Double.isFinite(edited.getPrice()) || edited.getPrice() < 0)) {
            throw new IllegalArgumentException("Цена должна быть числом от 0 или пустой");
        }
        edited.setAuthorId(current.getAuthorId());
        edited.setPublisherId(current.getPublisherId());
        edited.setGenreId(current.getGenreId());
        edited.setCycleId(current.getCycleId());
        edited.setAudioBookSiteIds(current.getAudioBookSiteIds());
        books.saveWithDates(edited, dates);
        return books.getById(edited.getId());
    }
}
