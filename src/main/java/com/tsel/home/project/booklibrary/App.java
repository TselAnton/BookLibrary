package com.tsel.home.project.booklibrary;

import static com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository.DEFAULT_REPOSITORY_PATH;
import static com.tsel.home.project.booklibrary.utils.FileUtils.buildPathFromCurrentDir;

import com.tsel.home.project.booklibrary.controller.impl.MainViewController;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.helper.DateProvider;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.helper.PropertyProvider;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import com.tsel.home.project.booklibrary.helper.StorageBackupArchiver;
import com.tsel.home.project.booklibrary.ui.StartupUpdateCheck;
import com.tsel.home.project.booklibrary.update.ApplicationPaths;
import com.tsel.home.project.booklibrary.update.GitHubReleaseClient;
import com.tsel.home.project.booklibrary.update.UpdateStartup;
import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import com.tsel.home.project.booklibrary.utils.Timer;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {
        JavaFXRunner.main(args);
    }

    public static class JavaFXRunner extends Application {

        private static final Path BACKUP_PATH = buildPathFromCurrentDir("backup");

        public static void main(String[] args) {
            try {
                if (!UpdateStartup.allowLaunch()) {
                    return;
                }
                InstallationLock lock = InstallationLock.acquire(ApplicationPaths.dataDirectory());
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        lock.close();
                    } catch (Exception e) {
                        log.error("Can't release application lock", e);
                    }
                }, "booklibrary-instance-lock"));
                if (!UpdateStartup.allowLaunch(true)) {
                    return;
                }
                initApplicationContext();
                launch(args);
            } catch (Exception e) {
                log.error("Can't start application", e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "BookLibrary — ошибка запуска", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static void initApplicationContext() {
            SimpleApplicationContext.initBean(PropertyProvider.class, () -> new PropertyProvider(buildPathFromCurrentDir()));

            SimpleApplicationContext.initBean(AudioBookSiteRepository.class, AudioBookSiteRepository::new);
            SimpleApplicationContext.initBean(AuthorRepository.class, AuthorRepository::new);
            SimpleApplicationContext.initBean(CycleRepository.class, CycleRepository::new);
            SimpleApplicationContext.initBean(GenreRepository.class, GenreRepository::new);
            SimpleApplicationContext.initBean(PublisherRepository.class, PublisherRepository::new);
            SimpleApplicationContext.initBean(UserSettingsRepository.class, UserSettingsRepository::new);
            SimpleApplicationContext.initBean(BookRepository.class, BookRepository::new);

            SimpleApplicationContext.initBean(ImageProvider.class, ImageProvider::new);
            SimpleApplicationContext.initBean(TableScroll.class, TableScroll::new);
            SimpleApplicationContext.initBean(DateProvider.class, DateProvider::new);

            Timer timer = Timer.start("Archive storages");
            StorageBackupArchiver storageBackupArchiver = new StorageBackupArchiver();
            storageBackupArchiver.archiveStorages(DEFAULT_REPOSITORY_PATH, BACKUP_PATH);
            timer.stop();

            timer = Timer.start("Remove expired storages");
            storageBackupArchiver.removeExpiredArchives(BACKUP_PATH);
            timer.stop();
        }

        @Override
        public void start(Stage primaryStage) {
            try {
                MainViewController mainViewController = new MainViewController();
                mainViewController.startScene(primaryStage);
                StartupUpdateCheck.start(primaryStage, new GitHubReleaseClient());

            } catch (Exception e) {
                log.error("Can't start main window", e);
            }
        }

        @Override
        public void stop() {
            SimpleApplicationContext.getBean(ImageProvider.class).close();
        }
    }
}
