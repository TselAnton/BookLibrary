package com.tsel.home.project.booklibrary.controller.impl.book;

import static com.tsel.home.project.booklibrary.utils.NumericConvertUtils.stringToDouble;
import static com.tsel.home.project.booklibrary.utils.NumericConvertUtils.stringToInteger;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.WARNING;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import com.tsel.home.project.booklibrary.dto.AuthorDTO;
import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import com.tsel.home.project.booklibrary.dto.CycleDTO;
import com.tsel.home.project.booklibrary.dto.PublisherDTO;
import com.tsel.home.project.booklibrary.utils.elements.CustomFileChooser;
import com.tsel.home.project.booklibrary.utils.MyGson;
import com.tsel.home.project.booklibrary.utils.table.AutoCompleteComboBoxListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractBookController extends AbstractViewController {

    private static final Logger log = LogManager.getLogger(AbstractBookController.class);

    private static final Gson GSON = MyGson.buildGson();

    private File defaultFolder;
    private CustomFileChooser fileChooser;

    /**
     * @return {@link TextField} для заполнения названия книги
     */
    protected abstract TextField getNameTextFieldInput();

    /**
     * @return {@link TextField} для заполнения количества страниц
     */
    protected abstract TextField getPagesCountFieldInput();

    /**
     * @return {@link TextField} для заполнения стоимости книги
     */
    protected abstract TextField getPriceFieldInput();

    /**
     * @return {@link CheckBox} для флага, если книга прочитана
     */
    protected abstract CheckBox getReadCheckBox();

    /**
     * @return {@link CheckBox} для флага, если у книги есть автограф
     */
    protected abstract CheckBox getAutographCheckBox();

    /**
     * @return {@link CheckBox} для флага, если у книги твёрдая обложка
     */
    protected abstract CheckBox getHardCoverCheckBox();

    /**
     * @return {@link ComboBox} с авторами
     */
    protected abstract ComboBox<ComboBoxDTO> getAuthorComboBox();

    /**
     * @return {@link ComboBox} с публицистами
     */
    protected abstract ComboBox<ComboBoxDTO> getPublisherComboBox();

    /**
     * @return {@link ComboBox} с циклами
     */
    protected abstract ComboBox<ComboBoxDTO> getCycleComboBox();

    /**
     * @return {@link CheckBox} для флага, если цикл является законченным
     */
    protected abstract CheckBox getCycleEndedCheckBox();

    /**
     * @return {@link TextField} для заполнения количества книг в цикле
     */
    protected abstract TextField getTotalCountInCycleFieldInput();

    /**
     * @return {@link TextField} для заполнения номера в цикле
     */
    protected abstract TextField getNumberInCycleFieldInput();

    /**
     * @return {@link Button} кнопки для выбора файла
     */
    protected abstract Button getSelectCoverFileButton();

    /**
     * @return {@link TextField} для заполнения пути к файлу - обложки книги
     */
    protected abstract TextField getCoverImagePathFieldInput();

    /**
     * @return {@link Button} кнопка выхода из модального окна
     */
    protected abstract Button getExitButton();

    /**
     * @return {@link Button} кнопка сохранения книги
     */
    protected abstract Button getSaveBookButton();


    /**
     * Инициализация дефолтных полей контроллера
     */
    protected void initController() {
        // Инициализация пикера обложек
        defaultFolder = new File(System.getProperty("user.dir"));
        fileChooser = new CustomFileChooser("Выбрать обложку книги", USER_SETTINGS_REPOSITORY_V2.getLastChosenCoverFile());

        // Инициализация выпадаек
        getAuthorComboBox().setItems(
            getComboBoxValues(AUTHOR_REPOSITORY_V2, author -> new AuthorDTO(author.getId(), author.getName()))
        );
        getPublisherComboBox().setItems(
            getComboBoxValues(PUBLISHER_REPOSITORY_V2, publisher -> new PublisherDTO(publisher.getId(), publisher.getName()))
        );
        getCycleComboBox().setItems(
            getComboBoxValues(CYCLE_REPOSITORY_V2, cycle -> new CycleDTO(cycle.getId(), cycle.getName()))
        );

        // Отдельно необходимо обновлять поля при изменении выбранного цикла книги
        getCycleComboBox().setOnAction(event -> updateCycleInformation());

        // Оборачиваем ComboBox в автозаполняемые
        AutoCompleteComboBoxListener.wrapComboBox(getAuthorComboBox());
        AutoCompleteComboBoxListener.wrapComboBox(getPublisherComboBox());
        AutoCompleteComboBoxListener.wrapComboBox(getCycleComboBox());
    }

    private <E extends BaseEntity<UUID>> ObservableList<ComboBoxDTO> getComboBoxValues(
        FileRepository<UUID, E> repository,
        Function<E, ComboBoxDTO> mapFunc
    ) {
        return observableArrayList(
            repository.getAll()
                .stream()
                .map(mapFunc)
                .toList()
        );
    }

    private void updateCycleInformation() {
        UUID itemId = getSelectedComboBoxItemId(getCycleComboBox(), CycleDTO::new);
        if (itemId != null) {
            Cycle cycle = CYCLE_REPOSITORY_V2.getById(itemId);
            if (cycle != null) {
                getCycleEndedCheckBox().setSelected(TRUE.equals(cycle.getEnded()));
                getTotalCountInCycleFieldInput().setText(String.valueOf(cycle.getBooksInCycle()));
            }
        }
    }

    /**
     * Выбор файл в качестве обложки книги
     */
    protected void selectBookCover() {
        Stage stage = (Stage) getSelectCoverFileButton().getScene().getWindow();

        File lastChosenFolderPath = USER_SETTINGS_REPOSITORY_V2.getLastChosenCoverFile();

        if (lastChosenFolderPath == null) {
            fileChooser.setInitialDirectory(defaultFolder);
        } else {
            fileChooser.setInitialDirectory(lastChosenFolderPath);
        }

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            log.warn("Not selected any file as book cover");
            return;
        }

        USER_SETTINGS_REPOSITORY_V2.updateLastChosenCoverFile(file);
        if (isValidImage(file.getAbsolutePath())) {
            getCoverImagePathFieldInput().setText(file.getAbsolutePath());
        } else {
            riseAlert(
                WARNING,
                "Ошибка",
                "Указанный файл невозможно открыть как картинку",
                "Файл имеет не поддерживаемое расширение или повреждён"
            );
        }
    }

    private boolean isValidImage(String absolutePath) {
        Path imgPath = Paths.get(absolutePath);
        if (Files.exists(imgPath)) {
            try (InputStream inputStream = Files.newInputStream(imgPath)) {
                Image bookImage = new Image(inputStream);
                return !bookImage.isError();

            } catch (IOException e) {
                log.error(format("Exception while load img %s", absolutePath), e);
            }
        }

        return false;
    }

    /**
     * Сохранение новой книги
     * @param audioBookSiteIds Идентификаторы сайтов аудиокниг
     */
    protected void saveNewBook(@Nullable UUID bookId, List<UUID> audioBookSiteIds) {
        String bookTitle = getInputText(getNameTextFieldInput());
        String bookPagesCount = getInputText(getPagesCountFieldInput());
        String bookPrice = getInputText(getPriceFieldInput());
        boolean readBookFlag = isChecked(getReadCheckBox());
        boolean hardCoverFlag = isChecked(getHardCoverCheckBox());

        ComboBoxDTO author = getSelectedComboBox(getAuthorComboBox(), AuthorDTO::new);
        ComboBoxDTO publisher = getSelectedComboBox(getPublisherComboBox(), PublisherDTO::new);

        ComboBoxDTO cycle = getSelectedComboBox(getCycleComboBox(), CycleDTO::new);
        boolean cycleEndedFlag = isChecked(getCycleEndedCheckBox());
        String cycleBookNumber = getInputText(getNumberInCycleFieldInput());
        String cycleTotalBookCount = getInputText(getTotalCountInCycleFieldInput());

        String imagePath = getInputText(getCoverImagePathFieldInput());

        if (isBookFieldsInvalid(bookTitle, bookPagesCount, bookPrice)
            || isAuthorAndPublisherFieldsInvalid(author, publisher)
            || isCycleFieldsInvalid(cycle, cycleBookNumber, cycleTotalBookCount, cycleEndedFlag)) {
            return;
        }

        Book newBook = null;

        try {
            AUTHOR_REPOSITORY_V2.beginTransaction();
            PUBLISHER_REPOSITORY_V2.beginTransaction();
            CYCLE_REPOSITORY_V2.beginTransaction();
            BOOK_REPOSITORY_V2.beginTransaction();

            Author newAuthor = new Author(author.getId(), author.getName());
            AUTHOR_REPOSITORY_V2.save(newAuthor);

            Publisher newPublisher = new Publisher(publisher.getId(), publisher.getName());
            PUBLISHER_REPOSITORY_V2.save(newPublisher);

            Cycle newCycle = null;
            if (cycle != null) {
                newCycle = Cycle.builder()
                    .id(cycle.getId())
                    .name(cycle.getName())
                    .ended(cycleEndedFlag)
                    .booksInCycle(stringToInteger(cycleTotalBookCount))
                    .build();

                CYCLE_REPOSITORY_V2.save(newCycle);
            }

            newBook = Book.builder()
                .id(bookId)
                .name(bookTitle)
                .authorId(newAuthor.getId())
                .publisherId(newPublisher.getId())
                .pages(stringToInteger(bookPagesCount))
                .read(readBookFlag)
                .autograph(isChecked(getAutographCheckBox()))
                .cycleId(newCycle != null ? newCycle.getId() : null)
                .numberInSeries(stringToInteger(cycleBookNumber))
                .coverImgAbsolutePath(imagePath)
                .price(stringToDouble(bookPrice))
                .hardCover(hardCoverFlag)
                .audioBookSiteIds(audioBookSiteIds)
                .build();

            BOOK_REPOSITORY_V2.save(newBook);

            BOOK_REPOSITORY_V2.commitTransaction();
            CYCLE_REPOSITORY_V2.commitTransaction();
            AUTHOR_REPOSITORY_V2.commitTransaction();
            PUBLISHER_REPOSITORY_V2.commitTransaction();

            doAfterSave(newBook);

            Stage stage = (Stage) getSaveBookButton().getScene().getWindow();
            stage.close();

        } catch (ConstraintException e) {
            log.warn("Constraint exception while trying to save new book '{}'", GSON.toJson(newBook), e);

            AUTHOR_REPOSITORY_V2.abortTransaction();
            PUBLISHER_REPOSITORY_V2.abortTransaction();
            CYCLE_REPOSITORY_V2.abortTransaction();
            BOOK_REPOSITORY_V2.abortTransaction();

            riseAlert(WARNING, "Ошибка", "Ошибка при создании новой книги", e.getMessage());

        } catch (Exception e) {
            log.error("Exception while trying to save new book '{}'", GSON.toJson(newBook), e);

            AUTHOR_REPOSITORY_V2.abortTransaction();
            PUBLISHER_REPOSITORY_V2.abortTransaction();
            CYCLE_REPOSITORY_V2.abortTransaction();
            BOOK_REPOSITORY_V2.abortTransaction();

            riseAlert(WARNING, "Ошибка", "Ошибка при создании новой книги", "Произошда непредвиденная ошибка: " + e.getMessage());
        }
    }

    protected void doAfterSave(Book savedBook) {
        // FOR OVERWRITE
    }

    private ComboBoxDTO getSelectedComboBox(ComboBox<?> comboBox, Function<String, ComboBoxDTO> comboBoxDTOFunction) {
        Object comboBoxObject = comboBox.getSelectionModel().getSelectedItem();
        if (comboBoxObject instanceof String stringValue) {
            return comboBoxDTOFunction.apply(stringValue);
        }
        if (comboBoxObject instanceof ComboBoxDTO comboBoxDTO) {
            return comboBoxDTO;
        }
        return null;
    }

    private UUID getSelectedComboBoxItemId(ComboBox<ComboBoxDTO> comboBox, Function<String, ComboBoxDTO> comboBoxDTOFunction) {
        ComboBoxDTO comboBoxDTO = getSelectedComboBox(comboBox, comboBoxDTOFunction);
        return comboBoxDTO != null ? comboBoxDTO.getId() : null;
    }

    private String getInputText(TextField textField) {
        String textFieldText = textField.getText();
        return isNotBlank(textFieldText) ? textFieldText.trim() : null;
    }

    private boolean isChecked(CheckBox checkBox) {
        return checkBox.isSelected();
    }

    private boolean isBookFieldsInvalid(String bookTitle, String bookPagesCount, String bookPrice) {
        if (isBlank(bookTitle)) {
            riseAlert(WARNING, "Ошибка", "Название книги не заполнено", "Название книги не может быть пустым");
            return true;
        }
        if (isBlank(bookPrice)) {
            riseAlert(WARNING, "Ошибка", "Стоимость указана не верно", "В стоимости книги указаны не числовые символы");
            return true;
        }
        if (stringToDouble(bookPrice) == null) {
            riseAlert(WARNING, "Ошибка", "Стоимость указана не верно", "В стоимости книги указаны не числовые символы");
            return true;
        }
        if (isBlank(bookPagesCount)) {
            riseAlert(WARNING, "Ошибка", "Количество страниц не заполнено",
                "Количество страниц в книги не может быть пустым");
            return true;
        }
        if (stringToInteger(bookPagesCount) == null) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнено количество страниц",
                "Количество страниц должно быть записано в виде целого числа");
            return true;
        }
        return false;
    }

    private boolean isAuthorAndPublisherFieldsInvalid(ComboBoxDTO author, ComboBoxDTO publisher) {
        if (author == null || isBlank(author.getName())) {
            riseAlert(WARNING, "Ошибка", "Имя автора не заполнено", "Имя автора не может быть пустым");
            return true;
        }
        if (publisher == null || isBlank(publisher.getName())) {
            riseAlert(WARNING, "Ошибка", "Название издателя не заполнено", "Название издателя не может быть пустым");
            return true;
        }
        return false;
    }

    private boolean isCycleFieldsInvalid(ComboBoxDTO cycle, String cycleBookNumber, String cycleTotalBookCount, boolean cycleEndedFlag) {
        if (isInvalidIntegerValue(cycleBookNumber)) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнен номер книги в цикле",
                "Номер книги в цикле должен быть записан в виде целого числа");
            return true;
        }
        if (isInvalidIntegerValue(cycleTotalBookCount)) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнено количество книги в цикле",
                "Количество книг в цикле должно быть записано в виде целого числа");
            return true;
        }

        // Проверки случаев, когда цикл не указан, а описательные поля, связанные с циклом, указаны
        if (cycle == null || isBlank(cycle.getName())) {
            if (cycleEndedFlag) {
                riseAlert(WARNING, "Ошибка", "Название цикла не заполнено",
                    "Указано, что цикл окончен, но название цикла при этом не может быть пустым");
                return true;
            }
            if (isNotBlank(cycleBookNumber)) {
                riseAlert(WARNING, "Ошибка", "Название цикла не заполнено",
                    "Указан номер книги в цикле, но название цикла при этом не может быть пустым");
                return true;
            }
            if (isNotBlank(cycleTotalBookCount)) {
                riseAlert(WARNING, "Ошибка", "Название цикла не заполнено",
                    "Указано количество книг в цикле, но название цикла при этом не может быть пустым");
                return true;
            }

        // Проверка обратных случаев, когда цикл указан, а описательные поля, связанные с циклом, не указаны
        } else {
            if (isBlank(cycleBookNumber)) {
                riseAlert(WARNING, "Ошибка", "Неверно заполнен номер книги в цикле",
                    "Указано название цикла, но номер книги в цикле при этом не может быть пустым");
                return true;
            }
            if (isBlank(cycleTotalBookCount)) {
                riseAlert(WARNING, "Ошибка", "Неверно заполнено количество книги в цикле",
                    "Указано название цикла, но количество книг в цикле при этом не может быть пустым");
                return true;
            }
        }

        return false;
    }

    private static boolean isInvalidIntegerValue(String cycleBookNumber) {
        return isNotBlank(cycleBookNumber) && stringToInteger(cycleBookNumber) == null;
    }
}
