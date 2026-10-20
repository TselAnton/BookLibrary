package com.tsel.home.project.booklibrary.dao.schema;

import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.data.UserSettings;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.helper.FileRepositoryProvider;
import java.util.List;

public final class StorageSchemas {

    private StorageSchemas() {}

    public static List<Definition> definitions() {
        return List.of(
            definition(Book.class, BookRepository.class),
            definition(Author.class, AuthorRepository.class),
            definition(Genre.class, GenreRepository.class),
            definition(Publisher.class, PublisherRepository.class),
            definition(Cycle.class, CycleRepository.class),
            definition(AudioBookSite.class, AudioBookSiteRepository.class),
            definition(UserSettings.class, UserSettingsRepository.class)
        );
    }

    private static Definition definition(Class<?> entity, Class<?> repository) {
        return new Definition(new EntitySchema(entity), FileRepositoryProvider.resolveStorageFileName(repository));
    }

    public record Definition(EntitySchema schema, String fileName) {}
}
