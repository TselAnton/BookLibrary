package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepositoryV2;
import java.util.UUID;

public class AppRepositoryConverter {

    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY_V1 = AudioBookSiteRepository.getInstance();
    private static final AudioBookSiteRepositoryV2 AUDIO_BOOK_SITE_REPOSITORY_V2 = AudioBookSiteRepositoryV2.INSTANCE;

    private static final AuthorRepository AUTHOR_REPOSITORY_V1 = AuthorRepository.getInstance();
    private static final AuthorRepositoryV2 AUTHOR_REPOSITORY_V2 = AuthorRepositoryV2.INSTANCE;

    private static final CycleRepository CYCLE_REPOSITORY_V1 = CycleRepository.getInstance();
    private static final CycleRepositoryV2 CYCLE_REPOSITORY_V2 = CycleRepositoryV2.INSTANCE;

    private static final PublisherRepository PUBLISHER_REPOSITORY_V1 = PublisherRepository.getInstance();
    private static final PublisherRepositoryV2 PUBLISHER_REPOSITORY_V2 = PublisherRepositoryV2.INSTANCE;

    private static final BookRepository BOOK_REPOSITORY_V1 = BookRepository.getInstance();
    private static final BookRepositoryV2 BOOK_REPOSITORY_V2 = BookRepositoryV2.INSTANCE;

    private static final UserSettingsRepository USER_SETTINGS_REPOSITORY_V1 = UserSettingsRepository.getInstance();
    private static final UserSettingsRepositoryV2 USER_SETTINGS_REPOSITORY_V2 = UserSettingsRepositoryV2.INSTANCE;

    public static void main(String[] args) {
        AUDIO_BOOK_SITE_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addId)
            .forEach(AUDIO_BOOK_SITE_REPOSITORY_V2::save);

        AUTHOR_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addId)
            .forEach(AUTHOR_REPOSITORY_V2::save);

        CYCLE_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addId)
            .forEach(CYCLE_REPOSITORY_V2::save);

        PUBLISHER_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addId)
            .forEach(PUBLISHER_REPOSITORY_V2::save);

        BOOK_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addId)
            .map(AppRepositoryConverter::setIdsInsteadOfNames)
            .forEach(BOOK_REPOSITORY_V2::save);

        USER_SETTINGS_REPOSITORY_V1.getAll()
            .stream()
            .map(AppRepositoryConverter::addStringId)
            .forEach(USER_SETTINGS_REPOSITORY_V2::save);
    }

    private static <E extends BaseEntity<UUID>> E addId(E entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return entity;
    }

    private static Book setIdsInsteadOfNames(Book book) {
        if (book.getAuthor() != null) {
            Author author = AUTHOR_REPOSITORY_V1.getByName(book.getAuthor());
            book.setAuthorId(author.getId());
        }
        if (book.getPublisher() != null) {
            Publisher publisher = PUBLISHER_REPOSITORY_V1.getByName(book.getPublisher());
            book.setPublisherId(publisher.getId());
        }
        if (book.getCycleName() != null) {
            Cycle cycle = CYCLE_REPOSITORY_V1.getByName(book.getCycleName());
            book.setCycleId(cycle.getId());
        }
        return book;
    }

    private static <E extends BaseEntity<String>> E addStringId(E entity) {
        if (entity.getId() == null) {
            entity.setId(entity.getKey());
        }
        return entity;
    }
}