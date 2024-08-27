package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepositoryV2;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

public abstract class AbstractJavaFxTest {

    @TempDir
    protected static Path temporaryDirectory;

    protected static AudioBookSiteRepositoryV2 audioBookSiteRepository;
    protected static AuthorRepositoryV2 authorRepository;
    protected static BookRepositoryV2 bookRepositoryV2;
    protected static CycleRepositoryV2 cycleRepositoryV2;
    protected static PublisherRepositoryV2 publisherRepositoryV2;
    protected static UserSettingsRepositoryV2 userSettingsRepositoryV2;

    protected void initRepositories() {
        audioBookSiteRepository = AudioBookSiteRepositoryV2.getInstance(temporaryDirectory);
        authorRepository = AuthorRepositoryV2.getInstance(temporaryDirectory);
        bookRepositoryV2 = BookRepositoryV2.getInstance(temporaryDirectory);
        cycleRepositoryV2 = CycleRepositoryV2.getInstance(temporaryDirectory);
        publisherRepositoryV2 = PublisherRepositoryV2.getInstance(temporaryDirectory);
        userSettingsRepositoryV2 = UserSettingsRepositoryV2.getInstance(temporaryDirectory);
    }
}

