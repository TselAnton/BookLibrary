package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.TestDateProvider;
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

    protected static AudioBookSiteRepositoryV2 audioBookSiteRepository;
    protected static AuthorRepositoryV2 authorRepository;
    protected static BookRepositoryV2 bookRepositoryV2;
    protected static CycleRepositoryV2 cycleRepositoryV2;
    protected static PublisherRepositoryV2 publisherRepositoryV2;
    protected static UserSettingsRepositoryV2 userSettingsRepositoryV2;
    protected static TestDateProvider testDateProvider;

    protected void initRepositories(Path temporaryDirectory) {
        audioBookSiteRepository = new AudioBookSiteRepositoryV2(temporaryDirectory);
        authorRepository = new AuthorRepositoryV2(temporaryDirectory);
        cycleRepositoryV2 = new CycleRepositoryV2(temporaryDirectory);
        publisherRepositoryV2 = new PublisherRepositoryV2(temporaryDirectory);
        userSettingsRepositoryV2 = new UserSettingsRepositoryV2(temporaryDirectory);
        bookRepositoryV2 = new BookRepositoryV2(temporaryDirectory);

        testDateProvider = new TestDateProvider();

        SimpleApplicationContext.initBean(PropertyProvider.class, () -> new PropertyProvider(temporaryDirectory));

        SimpleApplicationContext.initBean(AudioBookSiteRepositoryV2.class, () -> audioBookSiteRepository);
        SimpleApplicationContext.initBean(AuthorRepositoryV2.class, () -> authorRepository);
        SimpleApplicationContext.initBean(CycleRepositoryV2.class, () -> cycleRepositoryV2);
        SimpleApplicationContext.initBean(PublisherRepositoryV2.class, () -> publisherRepositoryV2);
        SimpleApplicationContext.initBean(UserSettingsRepositoryV2.class, () -> userSettingsRepositoryV2);
        SimpleApplicationContext.initBean(BookRepositoryV2.class, () -> bookRepositoryV2);

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

