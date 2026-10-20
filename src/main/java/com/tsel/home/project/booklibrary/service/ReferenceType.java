package com.tsel.home.project.booklibrary.service;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import java.util.UUID;
import lombok.Getter;

@Getter
public enum ReferenceType {
    CYCLES("Правка циклов", Cycle.class),
    AUTHORS("Правка авторов", Author.class),
    GENRES("Правка жанров", Genre.class),
    PUBLISHERS("Правка публицистов", Publisher.class);

    private final String title;
    private final Class<? extends BaseEntity<UUID>> entityClass;

    ReferenceType(String title, Class<? extends BaseEntity<UUID>> entityClass) {
        this.title = title;
        this.entityClass = entityClass;
    }

    public UUID referenceId(Book book) {
        return switch (this) {
            case CYCLES -> book.getCycleId();
            case AUTHORS -> book.getAuthorId();
            case GENRES -> book.getGenreId();
            case PUBLISHERS -> book.getPublisherId();
        };
    }
}
