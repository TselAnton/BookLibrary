package com.tsel.home.project.booklibrary.controller.impl.book;

import static com.tsel.home.project.booklibrary.utils.NumericConvertUtils.stringToDouble;
import static com.tsel.home.project.booklibrary.utils.NumericConvertUtils.stringToInteger;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.WARNING;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.controller.AbstractViewController;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import com.tsel.home.project.booklibrary.dto.AuthorDTO;
import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import com.tsel.home.project.booklibrary.dto.CycleDTO;
import com.tsel.home.project.booklibrary.dto.GenreDTO;
import com.tsel.home.project.booklibrary.dto.PublisherDTO;
import com.tsel.home.project.booklibrary.service.BookSaveService;
import com.tsel.home.project.booklibrary.utils.MyGson;
import com.tsel.home.project.booklibrary.utils.table.AutoCompleteComboBoxListener;
import com.tsel.home.project.booklibrary.utils.table.CustomFileChooser;
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
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import com.tsel.home.project.booklibrary.ui.SageTheme;
import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBookController extends AbstractViewController {

    private static final Gson GSON = MyGson.buildGson();

    private File defaultFolder;
    private CustomFileChooser fileChooser;
    private List<String> initialValues = List.of();

    public void configureWindow(Stage stage) {
        stage.getScene().getRoot().applyCss();
        initialValues = formValues();
        stage.setOnCloseRequest(e -> { if (!canDiscard()) e.consume(); });
        stage.setOnShown(e -> {
            stage.getScene().getRoot().applyCss();
            initialValues = formValues();
            Platform.runLater(() -> getNameTextFieldInput().requestFocus());
        });
        List<Node> fields = List.of(getNameTextFieldInput(), getAuthorComboBox(), getPublisherComboBox(),
            getPagesCountFieldInput(), getPublicationYearFieldInput(), getHardCoverCheckBox(), getReadCheckBox(),
            getAutographCheckBox(), getGenreComboBox(), getCycleComboBox(), getCycleEndedCheckBox(),
            getNumberInCycleFieldInput(), getCoverImagePathFieldInput(), getPriceFieldInput());
        for (Node node : getMainAnchorPane().lookupAll(".form-label")) {
            Label label = (Label) node;
            Integer row = javafx.scene.layout.GridPane.getRowIndex(label);
            Node field = fields.get(row == null ? 0 : row);
            label.setLabelFor(field);
            field.setAccessibleText(label.getText().replace("*", ", обязательное поле"));
            if (label.getText().endsWith("*")) {
                Text marker = new Text("*");
                marker.fontProperty().bind(label.fontProperty());
                marker.getStyleClass().add("required-marker");
                label.setText(label.getText().substring(0, label.getText().length() - 1));
                label.setGraphic(marker);
                label.setContentDisplay(ContentDisplay.RIGHT);
                label.setGraphicTextGap(0);
            }
            if (field instanceof CheckBox check) label.setOnMouseClicked(e -> {
                if (!check.isDisabled()) { check.fire(); check.requestFocus(); }
            });
        }
    }

    private List<String> formValues() {
        List<String> values = new ArrayList<>();
        for (TextField f : List.of(getNameTextFieldInput(), getPagesCountFieldInput(), getPublicationYearFieldInput(),
            getPriceFieldInput(), getNumberInCycleFieldInput(), getTotalCountInCycleFieldInput(), getCoverImagePathFieldInput()))
            values.add(Objects.toString(f.getText(), ""));
        for (ComboBox<ComboBoxDTO> c : List.of(getAuthorComboBox(), getPublisherComboBox(), getGenreComboBox(), getCycleComboBox()))
            values.add(Objects.toString(c.getEditor().getText(), ""));
        for (CheckBox c : List.of(getReadCheckBox(), getAutographCheckBox(), getHardCoverCheckBox(), getCycleEndedCheckBox()))
            values.add(Boolean.toString(c.isSelected()));
        return values;
    }

    private boolean canDiscard() {
        return initialValues.equals(formValues()) || riseAlert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
            "Несохранённые изменения", "Закрыть без сохранения?", "Введённые изменения будут потеряны.").isOkAnswer();
    }

    protected void cancelForm() { SageTheme.requestClose((Stage) getExitButton().getScene().getWindow()); }

    protected void handleFormKey(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE) return;
        for (ComboBox<?> combo : List.of(getAuthorComboBox(), getPublisherComboBox(), getGenreComboBox(), getCycleComboBox())) {
            if (combo.isShowing()) { combo.hide(); event.consume(); return; }
        }
        cancelForm(); event.consume();
    }

    private boolean validateForm() {
        for (Node node : getMainAnchorPane().lookupAll(".invalid")) node.getStyleClass().remove("invalid");
        String message = null;
        Node invalid = null;
        if (isBlank(getNameTextFieldInput().getText())) { invalid = getNameTextFieldInput(); message = "Введите название книги"; }
        else if (isBlank(getAuthorComboBox().getEditor().getText())) { invalid = getAuthorComboBox(); message = "Введите имя автора"; }
        else if (isBlank(getPublisherComboBox().getEditor().getText())) { invalid = getPublisherComboBox(); message = "Введите название издателя"; }
        else {
            TextField[] numbers = {getPagesCountFieldInput(), getPublicationYearFieldInput(), getPriceFieldInput()};
            String[] names = {"Количество страниц", "Год издания", "Стоимость"};
            for (int i = 0; i < numbers.length; i++) {
                Double value = i == 2 ? stringToDouble(numbers[i].getText())
                    : stringToInteger(numbers[i].getText()) == null ? null : stringToInteger(numbers[i].getText()).doubleValue();
                if (value == null || !Double.isFinite(value) || value < 0) {
                    invalid = numbers[i]; message = names[i] + (i == 2 ? ": введите число от 0" : ": введите целое число от 0"); break;
                }
            }
        }
        if (invalid == null && isNotBlank(getCycleComboBox().getEditor().getText())) {
            TextField[] cycleFields = {getNumberInCycleFieldInput(), getTotalCountInCycleFieldInput()};
            String[] names = {"Номер в цикле", "Количество книг в цикле"};
            for (int i = 0; i < cycleFields.length; i++) {
                Integer value = stringToInteger(cycleFields[i].getText());
                if (value == null || value < 0) {
                    invalid = cycleFields[i]; message = names[i] + ": введите целое число от 0"; break;
                }
            }
        }
        Label status = (Label) getMainAnchorPane().lookup("#validationStatus");
        if (status != null) { status.setText(message == null ? "" : message); }
        if (invalid != null) {
            invalid.getStyleClass().add("invalid"); invalid.setAccessibleHelp(message); invalid.requestFocus(); return false;
        }
        return true;
    }


    /**
     * @return {@link TextField} для заполнения названия книги
     */
    protected abstract TextField getNameTextFieldInput();

    /**
     * @return {@link TextField} для заполнения количества страниц
     */
    protected abstract TextField getPagesCountFieldInput();

    /**
     * @return {@link TextField} для заполнения года выпуска книги
     */
    protected abstract TextField getPublicationYearFieldInput();

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
     * @return {@link ComboBox} с жанрами
     */
    protected abstract ComboBox<ComboBoxDTO> getGenreComboBox();

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
        getPriceFieldInput().setTextFormatter(new javafx.scene.control.TextFormatter<>(change ->
            change.getControlNewText().matches("[0-9]*") ? change : null));
        // Инициализация пикера обложек
        defaultFolder = new File(System.getProperty("user.dir"));
        fileChooser = new CustomFileChooser("Выбрать обложку книги", userSettingsRepository.getLastChosenCoverFile());

        // Инициализация выпадаек
        getAuthorComboBox().setItems(
            getComboBoxValues(authorRepository, author -> new AuthorDTO(author.getId(), author.getName()))
        );
        getPublisherComboBox().setItems(
            getComboBoxValues(publisherRepository, publisher -> new PublisherDTO(publisher.getId(), publisher.getName()))
        );
        getCycleComboBox().setItems(
            getComboBoxValues(cycleRepository, cycle -> new CycleDTO(cycle.getId(), cycle.getName()))
        );
        getGenreComboBox().setItems(
            getComboBoxValues(genreRepository, genre -> new GenreDTO(genre.getId(), genre.getName()))
        );

        // Отдельно необходимо обновлять поля при изменении выбранного цикла книги
        getCycleComboBox().setOnAction(event -> updateCycleInformation());

        // Оборачиваем ComboBox в автозаполняемые
        AutoCompleteComboBoxListener.wrapComboBox(getAuthorComboBox());
        AutoCompleteComboBoxListener.wrapComboBox(getPublisherComboBox());
        AutoCompleteComboBoxListener.wrapComboBox(getGenreComboBox());
        AutoCompleteComboBoxListener.wrapComboBox(getCycleComboBox());
        getCycleComboBox().getEditor().textProperty().addListener((o, a, b) -> updateCycleAvailability());
        updateCycleAvailability();
        for (ComboBox<ComboBoxDTO> combo : List.of(getAuthorComboBox(), getPublisherComboBox(), getGenreComboBox(), getCycleComboBox()))
            SageTheme.constrainComboBox(combo);
    }

    protected String formatPrice(Double price) {
        return price == null ? "" : java.math.BigDecimal.valueOf(price)
            .setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    protected Double resolvePriceForSave(String price) { return stringToDouble(price); }

    private void updateCycleAvailability() {
        boolean absent = isBlank(getCycleComboBox().getEditor().getText());
        getCycleEndedCheckBox().setDisable(absent);
        getNumberInCycleFieldInput().setDisable(absent);
        getTotalCountInCycleFieldInput().setDisable(absent);
        if (absent) {
            getCycleEndedCheckBox().setSelected(false);
            getNumberInCycleFieldInput().clear(); getTotalCountInCycleFieldInput().clear();
        }
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
            Cycle cycle = cycleRepository.getById(itemId);
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

        File lastChosenFolderPath = userSettingsRepository.getLastChosenCoverFile();

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

        userSettingsRepository.updateLastChosenCoverFile(file);
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
     * @param bookId ID сохраняемой книги
     * @param audioBookSiteIds Идентификаторы сайтов аудиокниг
     */
    protected void saveNewBook(@Nullable UUID bookId, List<UUID> audioBookSiteIds) {
        if (!validateForm()) return;
        String bookTitle = getInputText(getNameTextFieldInput());
        String bookPagesCount = getInputText(getPagesCountFieldInput());
        String bookPublishYear = getInputText(getPublicationYearFieldInput());
        String bookPrice = getInputText(getPriceFieldInput());
        boolean readBookFlag = isChecked(getReadCheckBox());
        boolean hardCoverFlag = isChecked(getHardCoverCheckBox());

        ComboBoxDTO author = getSelectedComboBox(getAuthorComboBox(), AuthorDTO::new);
        ComboBoxDTO publisher = getSelectedComboBox(getPublisherComboBox(), PublisherDTO::new);
        ComboBoxDTO genre = getSelectedComboBox(getGenreComboBox(), GenreDTO::new);

        ComboBoxDTO cycle = getSelectedComboBox(getCycleComboBox(), CycleDTO::new);
        boolean cycleEndedFlag = isChecked(getCycleEndedCheckBox());
        String cycleBookNumber = getInputText(getNumberInCycleFieldInput());
        String cycleTotalBookCount = getInputText(getTotalCountInCycleFieldInput());

        String imagePath = getInputText(getCoverImagePathFieldInput());

        if (isBookFieldsInvalid(bookTitle, bookPagesCount, bookPrice, bookPublishYear)
            || isAuthorAndPublisherFieldsInvalid(author, publisher)
            || author == null || publisher == null
            || isCycleFieldsInvalid(cycle, cycleBookNumber, cycleTotalBookCount, cycleEndedFlag)) {
            return;
        }

        Book newBook = null;

        try {
            Author newAuthor = resolveAuthorByComboBox(author);
            Publisher newPublisher = resolvePublisherByComboBox(publisher);
            Genre newGenre = resolveGenreByComboBox(genre);
            Cycle newCycle = resolveCycleByComboBox(cycle, cycleEndedFlag, stringToInteger(cycleTotalBookCount));

            newBook = Book.builder()
                .id(bookId)
                .name(bookTitle)
                .authorId(newAuthor.getId())
                .publisherId(newPublisher.getId())
                .genreId(newGenre != null ? newGenre.getId() : null)
                .pages(stringToInteger(bookPagesCount))
                .publicationYear(stringToInteger(bookPublishYear))
                .read(readBookFlag)
                .autograph(isChecked(getAutographCheckBox()))
                .cycleId(newCycle != null ? newCycle.getId() : null)
                .numberInSeries(newCycle != null ? stringToInteger(cycleBookNumber) : null)
                .coverImgAbsolutePath(imagePath)
                .price(resolvePriceForSave(bookPrice))
                .hardCover(hardCoverFlag)
                .audioBookSiteIds(audioBookSiteIds)
                .build();

            BookSaveService service = new BookSaveService(
                bookRepository,
                authorRepository,
                publisherRepository,
                genreRepository,
                cycleRepository
            );
            newBook = service.save(newBook, newAuthor, newPublisher, newGenre, newCycle);

            doAfterSave(newBook);

            Stage stage = (Stage) getSaveBookButton().getScene().getWindow();
            stage.close();

        } catch (ConstraintException e) {
            log.warn("Constraint exception while trying to save new book '{}'", GSON.toJson(newBook), e);

            riseAlert(WARNING, "Ошибка", "Ошибка при создании новой книги", e.getMessage());

        } catch (Exception e) {
            log.error("Exception while trying to save new book '{}'", GSON.toJson(newBook), e);

            riseAlert(WARNING, "Ошибка", "Ошибка при создании новой книги", "Произошла непредвиденная ошибка: " + e.getMessage());
        }
    }

    protected void doAfterSave(Book savedBook) {
        // FOR OVERWRITE
    }

    private ComboBoxDTO getSelectedComboBox(ComboBox<?> comboBox, Function<String, ComboBoxDTO> comboBoxDTOFunction) {
        String typed = comboBox.isEditable() ? comboBox.getEditor().getText() : null;
        if (comboBox.isEditable() && isBlank(typed)) return null;
        Object comboBoxObject = comboBox.getSelectionModel().getSelectedItem();
        if (comboBox.isEditable() && (!(comboBoxObject instanceof ComboBoxDTO dto) || !typed.equals(dto.getName())))
            return comboBoxDTOFunction.apply(typed);
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

    private boolean isBookFieldsInvalid(String bookTitle, String bookPagesCount, String bookPrice, String bookPublishYear) {
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
        if (isBlank(bookPublishYear)) {
            riseAlert(WARNING, "Ошибка", "Не заполнен год выпуска книги",
                "Год выпуска книги не может быть пустым");
            return true;
        }
        Integer bookPublishYearInt = stringToInteger(bookPublishYear);
        if (bookPublishYearInt == null) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнен год выпуска книги",
                "Год выпуска книги должен быть записан в виде целого числа");
            return true;
        }
        if (bookPublishYearInt < 0) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнен год выпуска книги",
                "Год не может быть отрицательным числом");
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
        if (isInvalidIntegerValue(cycleBookNumber) || (stringToInteger(cycleBookNumber) != null && stringToInteger(cycleBookNumber) < 0)) {
            riseAlert(WARNING, "Ошибка", "Неверно заполнен номер книги в цикле",
                "Номер книги в цикле должен быть записан в виде целого числа");
            return true;
        }
        if (isInvalidIntegerValue(cycleTotalBookCount) || (stringToInteger(cycleTotalBookCount) != null && stringToInteger(cycleTotalBookCount) < 0)) {
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

    private Author resolveAuthorByComboBox(ComboBoxDTO authorComboBox) {
        return authorComboBox.getId() != null
            ? new Author(authorComboBox.getId(), authorComboBox.getName())
            : authorRepository.getByName(authorComboBox.getName()).orElse(new Author(null, authorComboBox.getName()));
    }

    private Publisher resolvePublisherByComboBox(ComboBoxDTO publisherComboBox) {
        return publisherComboBox.getId() != null
            ? new Publisher(publisherComboBox.getId(), publisherComboBox.getName())
            : publisherRepository.getByName(publisherComboBox.getName()).orElse(new Publisher(null, publisherComboBox.getName()));
    }

    private Genre resolveGenreByComboBox(ComboBoxDTO genreComboBox) {
        if (genreComboBox == null || isBlank(genreComboBox.getName())) {
            return null;
        }
        return genreComboBox.getId() != null
            ? new Genre(genreComboBox.getId(), genreComboBox.getName())
            : genreRepository.getByName(genreComboBox.getName()).orElse(new Genre(null, genreComboBox.getName()));
    }

    private Cycle resolveCycleByComboBox(ComboBoxDTO cycleComboBox, boolean isCycleEnded, Integer booksInCycle) {
        if (cycleComboBox == null || isBlank(cycleComboBox.getName())) {
            return null;
        }
        if (cycleComboBox.getId() != null) {
            return new Cycle(cycleComboBox.getId(), cycleComboBox.getName(), isCycleEnded, booksInCycle);
        }

        return cycleRepository.getByName(cycleComboBox.getName())
            .map(existedCycle -> {
                existedCycle.setEnded(isCycleEnded);
                existedCycle.setBooksInCycle(booksInCycle);
                return existedCycle;
            })
            .orElse(new Cycle(null, cycleComboBox.getName(), isCycleEnded, booksInCycle));
    }

}
