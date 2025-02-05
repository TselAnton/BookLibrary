package com.tsel.home.project.booklibrary.javafx;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import com.tsel.home.project.booklibrary.App;
import com.tsel.home.project.booklibrary.CheckBoxMatcher;
import com.tsel.home.project.booklibrary.ComboBoxMatcher;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@Disabled
@ExtendWith(ApplicationExtension.class)
class CreateBookTests extends AbstractJavaFxTest {

    @Start
    private void start(Stage stage) {
        initRepositories(temporaryDirectory);

        new App.JavaFXRunner().start(stage);
    }

    @AfterEach
    public void clearRepositories() {
        audioBookSiteRepository.getAll().forEach(audioBookSiteRepository::delete);
        authorRepository.getAll().forEach(authorRepository::delete);
        cycleRepositoryV2.getAll().forEach(cycleRepositoryV2::delete);
        publisherRepositoryV2.getAll().forEach(publisherRepositoryV2::delete);
        userSettingsRepositoryV2.getAll().forEach(userSettingsRepositoryV2::delete);
        bookRepositoryV2.getAll().forEach(bookRepositoryV2::delete);
    }

    @Test
    void validateTabTest(FxRobot robot) {
        // Нажатие "Добавить книгу"
        robot.clickOn("#addBookButton");

        List<String> expectedElementsOrder = new ArrayList<>(List.of(
            "#nameTextFieldInput",
            "#authorComboBox",
            "#publisherComboBox",
            "#pagesCountFieldInput",
            "#hardCoverCheckBox",
            "#readCheckBox",
            "#autographCheckBox",
            "#cycleComboBox",
            "#cycleEndedCheckBox",
            "#numberInCycleFieldInput",
            "#totalCountInCycleFieldInput",
            "#coverImagePathFieldInput",
            "#selectCoverFileButton",
            "#priceFieldInput",
            "#saveBookButton"
        ));

        // Нажимается TAB и проверяется, что фокусировка элементов идут друг за другом
        String beforeElement = null;
        for (String nextElement : expectedElementsOrder) {
            String finalBeforeElement = beforeElement;
            FxAssert.verifyThat(nextElement, Node::isFocused, errorMsgBuilder ->
                errorMsgBuilder
                    .append(". Element ")
                    .append(nextElement)
                    .append(" should be focused ")
                    .append(finalBeforeElement == null ? "first" : "after " + finalBeforeElement)
            );

            robot.press(KeyCode.TAB);
            robot.release(KeyCode.TAB);
            beforeElement = nextElement;
        }
    }

    @Test
    void createNewBookWithNewValuesTest(FxRobot robot) {
        UUID audioBookSiteId = audioBookSiteRepository.save(new AudioBookSite(UUID.randomUUID(), "TEST NEW AUDIO_BOOK_SITE 1"));

        // Нажатие "Добавить книгу"
        robot.clickOn("#addBookButton");

        // Проверка, что галочка на "твёрдой обложке" true по дефолту
        FxAssert.verifyThat("#hardCoverCheckBox", CheckBoxMatcher.isSelected());

        // Заполнение полей книги
        fillInTextField("#nameTextFieldInput", "TEST NEW BOOK", robot);
        fillInTextField("#authorComboBox", "TEST NEW AUTHOR", robot);
        fillInTextField("#publisherComboBox", "TEST NEW PUBLISHER", robot);
        fillInNumberField("#pagesCountFieldInput", 1234, robot);

        robot.clickOn("#hardCoverCheckBox");    // Убираем галочку "твёрдая обложка"
        robot.clickOn("#readCheckBox");         // Устанавливаем галочку "прочитано"
        robot.clickOn("#autographCheckBox");    // Устанавливаем галочку "автограф"

        fillInTextField("#cycleComboBox", "TEST NEW CYCLE", robot);
        robot.clickOn("#cycleEndedCheckBox");   // Устанавливаем галочку "завершённый цикл"
        fillInNumberField("#numberInCycleFieldInput", 10, robot);
        fillInNumberField("#totalCountInCycleFieldInput", 20, robot);
        fillInNumberField("#priceFieldInput", 5678, robot);

        // Добавление аудиокниг
        robot.clickOn("#audioBookSitesButton");
        robot.clickOn("#selectAudioBookSiteCheckBox");
        robot.clickOn("#saveButton");

        // Сохранение книги
        robot.clickOn("#saveBookButton");

        // Проверка, что новая книга сохранена
        List<Book> bookList = bookRepositoryV2.getAll();
        assertThat(bookList).hasSize(1);

        Book savedBook = bookList.get(0);
        assertNotNull(savedBook.getId());
        assertEquals("TEST NEW BOOK", savedBook.getName());
        assertEquals(1234, savedBook.getPages().intValue());
        assertTrue(savedBook.getRead());
        assertTrue(savedBook.getAutograph());
        assertFalse(savedBook.getHardCover());
        assertEquals(5678d, savedBook.getPrice());
        assertEquals(10, savedBook.getNumberInSeries().intValue());
        assertEquals(10, savedBook.getNumberInSeries().intValue());
        assertNotNull(savedBook.getCreatedAt());
        assertNotNull(savedBook.getUpdatedAt());
        assertNotNull(savedBook.getReadUpdatedAt());

        assertNotNull(savedBook.getAudioBookSiteIds());
        assertThat(savedBook.getAudioBookSiteIds()).containsExactlyInAnyOrder(audioBookSiteId);

        assertNotNull(savedBook.getAuthorId());
        Author savedAuthor = authorRepository.getById(savedBook.getAuthorId());
        assertNotNull(savedAuthor);
        assertNotNull(savedAuthor.getId());
        assertEquals("TEST NEW AUTHOR", savedAuthor.getName());

        assertNotNull(savedBook.getPublisherId());
        Publisher savedPublisher = publisherRepositoryV2.getById(savedBook.getPublisherId());
        assertNotNull(savedPublisher);
        assertNotNull(savedPublisher.getId());
        assertEquals("TEST NEW PUBLISHER", savedPublisher.getName());

        assertNotNull(savedBook.getCycleId());
        Cycle savedCycle = cycleRepositoryV2.getById(savedBook.getCycleId());
        assertNotNull(savedCycle);
        assertNotNull(savedCycle.getId());
        assertEquals("TEST NEW CYCLE", savedCycle.getName());
        assertEquals(20, savedCycle.getBooksInCycle().intValue());
        assertTrue(savedCycle.getEnded());
    }

    @Test
    void createNewBookWithExistedValuesTest(FxRobot robot) {
        UUID authorId = authorRepository.save(new Author(UUID.randomUUID(), "EXISTED AUTHOR"));
        UUID publisherId = publisherRepositoryV2.save(new Publisher(UUID.randomUUID(), "EXISTED PUBLISHER"));
        UUID cycleId = cycleRepositoryV2.save(new Cycle(UUID.randomUUID(), "EXISTED CYCLE", true, 3));

        // Нажатие "Добавить книгу"
        robot.clickOn("#addBookButton");

        // Проверка, что галочка на "твёрдой обложке" true по дефолту
        FxAssert.verifyThat("#hardCoverCheckBox", CheckBoxMatcher.isSelected());

        // Проверка, что автор, публицист и цикл имеют доступные значения
        FxAssert.verifyThat("#authorComboBox", ComboBoxMatcher.contains("EXISTED AUTHOR"));
        FxAssert.verifyThat("#publisherComboBox", ComboBoxMatcher.contains("EXISTED PUBLISHER"));
        FxAssert.verifyThat("#cycleComboBox", ComboBoxMatcher.contains("EXISTED CYCLE"));

        // Заполнение полей книги
        fillInTextField("#nameTextFieldInput", "TEST NEW BOOK", robot);

        fillIntComboBoxText("#authorComboBox", "EXISTED AUTHOR", robot);
        fillIntComboBoxText("#publisherComboBox", "EXISTED PUBLISHER", robot);

        fillInNumberField("#pagesCountFieldInput", 1234, robot);

        robot.clickOn("#hardCoverCheckBox");    // Убираем галочку "твёрдая обложка"
        robot.clickOn("#readCheckBox");         // Устанавливаем галочку "прочитано"
        robot.clickOn("#autographCheckBox");    // Устанавливаем галочку "автограф"

        fillIntComboBoxText("#cycleComboBox", "EXISTED CYCLE", robot);
        robot.clickOn("#cycleEndedCheckBox");   // Устанавливаем галочку "завершённый цикл"
        fillInNumberField("#numberInCycleFieldInput", 10, robot);
        fillInNumberField("#totalCountInCycleFieldInput", 20, robot);
        fillInNumberField("#priceFieldInput", 5678, robot);

        // Сохранение книги
        robot.clickOn("#saveBookButton");

        // Проверка, что новая книга сохранена
        List<Book> bookList = bookRepositoryV2.getAll();
        assertThat(bookList).hasSize(1);

        Book savedBook = bookList.get(0);
        assertNotNull(savedBook.getId());
        assertEquals("TEST NEW BOOK", savedBook.getName());
        assertEquals(1234, savedBook.getPages().intValue());
        assertTrue(savedBook.getRead());
        assertTrue(savedBook.getAutograph());
        assertFalse(savedBook.getHardCover());
        assertEquals(5678d, savedBook.getPrice());
        assertEquals(10, savedBook.getNumberInSeries().intValue());
        assertEquals(10, savedBook.getNumberInSeries().intValue());
        assertNotNull(savedBook.getCreatedAt());
        assertNotNull(savedBook.getUpdatedAt());
        assertNotNull(savedBook.getReadUpdatedAt());

        // Проверка, что автор ID идентичный сохранённому в БД изначально, и не создалось дополнительных авторов
        assertNotNull(savedBook.getAuthorId());
        assertEquals(authorId, savedBook.getAuthorId());
        assertEquals(1, authorRepository.getAll().size());

        Author savedAuthor = authorRepository.getById(authorId);
        assertNotNull(savedAuthor);
        assertNotNull(savedAuthor.getId());
        assertEquals("EXISTED AUTHOR", savedAuthor.getName());

        // Проверка, что публицист ID идентичный сохранённому в БД изначально, и не создалось дополнительных публицистов
        assertNotNull(savedBook.getPublisherId());
        assertEquals(publisherId, savedBook.getPublisherId());
        assertEquals(1, publisherRepositoryV2.getAll().size());

        Publisher savedPublisher = publisherRepositoryV2.getById(publisherId);
        assertNotNull(savedPublisher);
        assertNotNull(savedPublisher.getId());
        assertEquals("EXISTED PUBLISHER", savedPublisher.getName());

        // Проверка, что цикл ID идентичный сохранённому в БД изначально, и не создалось дополнительных циклов
        assertNotNull(savedBook.getCycleId());
        assertEquals(cycleId, savedBook.getCycleId());
        assertEquals(1, cycleRepositoryV2.getAll().size());

        Cycle savedCycle = cycleRepositoryV2.getById(cycleId);
        assertNotNull(savedCycle);
        assertNotNull(savedCycle.getId());
        assertEquals("EXISTED CYCLE", savedCycle.getName());
        assertEquals(20, savedCycle.getBooksInCycle().intValue());
        assertTrue(savedCycle.getEnded());
    }

    // TODO: Создание идентичной сущности существующей, проверка валидации при создании сущности
}
