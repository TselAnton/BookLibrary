package com.tsel.home.project.booklibrary;

import static com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2.DEFAULT_REPOSITORY_PATH;
import static com.tsel.home.project.booklibrary.utils.FileUtils.buildPathFromCurrentDir;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepositoryV2;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepositoryV2;
import com.tsel.home.project.booklibrary.helper.DateProvider;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.helper.PropertyProvider;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import com.tsel.home.project.booklibrary.helper.StorageBackupArchiver;
import com.tsel.home.project.booklibrary.search.SearchService;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {
        JavaFXRunner.main(args);
    }

    public static class JavaFXRunner extends Application {

        private static final Path BACKUP_PATH = buildPathFromCurrentDir("backup");

        public static void main(String[] args) {
            initApplicationContext();
            launch(args);
        }

        private static void initApplicationContext() {
            SimpleApplicationContext.initBean(PropertyProvider.class, () -> new PropertyProvider(buildPathFromCurrentDir()));

            SimpleApplicationContext.initBean(AudioBookSiteRepositoryV2.class, AudioBookSiteRepositoryV2::new);
            SimpleApplicationContext.initBean(AuthorRepositoryV2.class, AuthorRepositoryV2::new);
            SimpleApplicationContext.initBean(CycleRepositoryV2.class, CycleRepositoryV2::new);
            SimpleApplicationContext.initBean(PublisherRepositoryV2.class, PublisherRepositoryV2::new);
            SimpleApplicationContext.initBean(UserSettingsRepositoryV2.class, UserSettingsRepositoryV2::new);
            SimpleApplicationContext.initBean(BookRepositoryV2.class, BookRepositoryV2::new);

            SimpleApplicationContext.initBean(ImageProvider.class, ImageProvider::new);
            SimpleApplicationContext.initBean(SearchService.class, SearchService::new);
            SimpleApplicationContext.initBean(TableScroll.class, TableScroll::new);
            SimpleApplicationContext.initBean(DateProvider.class, DateProvider::new);

            StorageBackupArchiver storageBackupArchiver = new StorageBackupArchiver();
            storageBackupArchiver.archiveStorages(DEFAULT_REPOSITORY_PATH, BACKUP_PATH);
            storageBackupArchiver.removeExpiredArchives(BACKUP_PATH);
        }

        @Override
        public void start(Stage primaryStage) {
            try {
                MainViewController mainViewController = new MainViewController();
                mainViewController.startScene(primaryStage);

            } catch (Exception e) {
                log.error("Can't start main window", e);
            }
        }
    }
}
