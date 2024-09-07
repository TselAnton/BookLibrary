package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.TestDateProvider;
import com.tsel.home.project.booklibrary.dao.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.helper.DateProvider;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.helper.PropertyProvider;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import com.tsel.home.project.booklibrary.search.SearchService;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.nio.file.Path;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;

// Тесты основанные на https://github.com/TestFX/TestFX
public abstract class AbstractJavaFxTest {

    @TempDir
    protected static Path temporaryDirectory;

    protected static AudioBookSiteRepository audioBookSiteRepository;
    protected static AuthorRepository authorRepository;
    protected static BookRepository bookRepositoryV2;
    protected static CycleRepository cycleRepositoryV2;
    protected static PublisherRepository publisherRepositoryV2;
    protected static UserSettingsRepository userSettingsRepositoryV2;
    protected static TestDateProvider testDateProvider;

    protected void initRepositories(Path temporaryDirectory) {
        audioBookSiteRepository = new AudioBookSiteRepository(temporaryDirectory);
        authorRepository = new AuthorRepository(temporaryDirectory);
        cycleRepositoryV2 = new CycleRepository(temporaryDirectory);
        publisherRepositoryV2 = new PublisherRepository(temporaryDirectory);
        userSettingsRepositoryV2 = new UserSettingsRepository(temporaryDirectory);
        bookRepositoryV2 = new BookRepository(temporaryDirectory);

        testDateProvider = new TestDateProvider();

        SimpleApplicationContext.initBean(PropertyProvider.class, () -> new PropertyProvider(temporaryDirectory));

        SimpleApplicationContext.initBean(AudioBookSiteRepository.class, () -> audioBookSiteRepository);
        SimpleApplicationContext.initBean(AuthorRepository.class, () -> authorRepository);
        SimpleApplicationContext.initBean(CycleRepository.class, () -> cycleRepositoryV2);
        SimpleApplicationContext.initBean(PublisherRepository.class, () -> publisherRepositoryV2);
        SimpleApplicationContext.initBean(UserSettingsRepository.class, () -> userSettingsRepositoryV2);
        SimpleApplicationContext.initBean(BookRepository.class, () -> bookRepositoryV2);

        SimpleApplicationContext.initBean(ImageProvider.class, ImageProvider::new);
        SimpleApplicationContext.initBean(SearchService.class, SearchService::new);
        SimpleApplicationContext.initBean(TableScroll.class, TableScroll::new);
        SimpleApplicationContext.initBean(DateProvider.class, () -> testDateProvider);
    }

    protected void fillInTextField(String elementName, String text, FxRobot robot) {
        robot.clickOn(elementName.startsWith("#") ? elementName : "#" + elementName);
        robot.write(text);
    }

    protected void fillInNumberField(String elementName, Number number, FxRobot robot) {
        robot.clickOn(elementName.startsWith("#") ? elementName : "#" + elementName);
        robot.write(String.valueOf(number));
    }

    protected void pressKey(KeyCode keyCode, FxRobot robot) {
        robot.press(keyCode);
        robot.release(keyCode);
    }

    protected void fillIntComboBoxText(String elementName, String text, FxRobot fxRobot) {
        fillInTextField(elementName, text, fxRobot);
        pressKey(KeyCode.TAB, fxRobot);
    }
}

