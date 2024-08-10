package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.repository.impl.AudioBookSiteRepositoryV2;
import com.tsel.home.project.booklibrary.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.repository.impl.AuthorRepositoryV2;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.BookRepositoryV2;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepositoryV2;
import com.tsel.home.project.booklibrary.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.repository.impl.PublisherRepositoryV2;
import com.tsel.home.project.booklibrary.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.repository.impl.UserSettingsRepositoryV2;

public class AppRepositoryConverter {

    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY_V1 = AudioBookSiteRepository.getInstance();
    private static final AudioBookSiteRepositoryV2 AUDIO_BOOK_SITE_REPOSITORY_V2 = AudioBookSiteRepositoryV2.getInstance();

    private static final AuthorRepository AUTHOR_REPOSITORY_V1 = AuthorRepository.getInstance();
    private static final AuthorRepositoryV2 AUTHOR_REPOSITORY_V2 = AuthorRepositoryV2.getInstance();

    private static final CycleRepository CYCLE_REPOSITORY_V1 = CycleRepository.getInstance();
    private static final CycleRepositoryV2 CYCLE_REPOSITORY_V2 = CycleRepositoryV2.getInstance();

    private static final PublisherRepository PUBLISHER_REPOSITORY_V1 = PublisherRepository.getInstance();
    private static final PublisherRepositoryV2 PUBLISHER_REPOSITORY_V2 = PublisherRepositoryV2.getInstance();

    private static final BookRepository BOOK_REPOSITORY_V1 = BookRepository.getInstance();
    private static final BookRepositoryV2 BOOK_REPOSITORY_V2 = BookRepositoryV2.getInstance();

    private static final UserSettingsRepository USER_SETTINGS_REPOSITORY_V1 = UserSettingsRepository.getInstance();
    private static final UserSettingsRepositoryV2 USER_SETTINGS_REPOSITORY_V2 = UserSettingsRepositoryV2.getInstance();

    // TODO: делать в отдельной дериктории
    public static void main(String[] args) {
        AUDIO_BOOK_SITE_REPOSITORY_V1.getAll().forEach(AUDIO_BOOK_SITE_REPOSITORY_V2::save);
        AUTHOR_REPOSITORY_V1.getAll().forEach(AUTHOR_REPOSITORY_V2::save);
        CYCLE_REPOSITORY_V1.getAll().forEach(CYCLE_REPOSITORY_V2::save);
        PUBLISHER_REPOSITORY_V1.getAll().forEach(PUBLISHER_REPOSITORY_V2::save);
        BOOK_REPOSITORY_V1.getAll().forEach(BOOK_REPOSITORY_V2::save);
        USER_SETTINGS_REPOSITORY_V1.getAll().forEach(USER_SETTINGS_REPOSITORY_V2::save);
    }
}
