package com.tsel.home.project.booklibrary.controller;

import com.tsel.home.project.booklibrary.builder.BookBuilder;
import com.tsel.home.project.booklibrary.builder.CycleBuilder;
import com.tsel.home.project.booklibrary.data.*;
import com.tsel.home.project.booklibrary.repository.FileRepository;
import com.tsel.home.project.booklibrary.utils.AutoCompleteComboBoxListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.WARNING;

public abstract class AbstractEditViewController extends AbstractViewController {

    protected static final FileChooser FILE_CHOOSER = new FileChooser();

    private File defaultFolder;

    public AbstractEditViewController() {
        super(null, null);
    }

    protected void init(ComboBox<String> authorInput,
                        ComboBox<String> publisherInput,
                        ComboBox<String> cycleInput,
                        CheckBox isEndedCycleCheckBox,
                        TextField totalCountInCycleInput) {

        FILE_CHOOSER.setTitle("Выбрать обложку книги");

        USER_SETTINGS_REPOSITORY.getLastChosenCoverFile()
                .ifPresent(FILE_CHOOSER::setInitialDirectory);

        defaultFolder = new File(System.getProperty("user.dir"));

        authorInput.setItems(initComboBoxValues(AUTHOR_REPOSITORY));
        publisherInput.setItems(initComboBoxValues(PUBLISHER_REPOSITORY));
        cycleInput.setItems(initComboBoxValues(CYCLE_REPOSITORY));

        cycleInput.setOnAction(event ->
                updateCycleInfo(cycleInput, isEndedCycleCheckBox, totalCountInCycleInput));

        new AutoCompleteComboBoxListener<>(authorInput);
        new AutoCompleteComboBoxListener<>(publisherInput);
        new AutoCompleteComboBoxListener<>(cycleInput);
    }

    private ObservableList<String> initComboBoxValues(FileRepository<?> repository) {
        return observableArrayList(
                repository.getAll()
                        .stream()
                        .map(BaseEntity::getKey)
                        .collect(toList())
        );
    }

    private void updateCycleInfo(ComboBox<String> cycleInput, CheckBox isEndedCycleCheckBox, TextField totalCountInCycleInput) {
        String cycleName = getInputText(cycleInput);
        if (isNotBlank(cycleName)) {
            Cycle cycle = CYCLE_REPOSITORY.getByName(cycleName);
            if (cycle != null) {
                isEndedCycleCheckBox.setSelected(TRUE.equals(cycle.getEnded()));
                totalCountInCycleInput.setText(String.valueOf(cycle.getBooksInCycle()));
            }
        }
    }

    protected void closeStage(Button cancelAddingButton) {
        Stage stage = (Stage) cancelAddingButton.getScene().getWindow();
        stage.close();
    }

    protected void selectFileAsCover(Button selectFileButton, TextField imagePathTextField) {
        Stage stage = (Stage) selectFileButton.getScene().getWindow();

        Optional<File> lastChosenFolderPath = USER_SETTINGS_REPOSITORY.getLastChosenCoverFile();

        if (lastChosenFolderPath.isEmpty()) {
            FILE_CHOOSER.setInitialDirectory(defaultFolder);
        } else {
            FILE_CHOOSER.setInitialDirectory(lastChosenFolderPath.get());
        }

        File file = FILE_CHOOSER.showOpenDialog(stage);
        if (file != null) {
            USER_SETTINGS_REPOSITORY.updateLastChosenCoverFile(file);

            if (isValidImage(file.getAbsolutePath())) {
                imagePathTextField.setText(file.getAbsolutePath());
            } else {
                riseAlert(WARNING, "Ошибка", "Указанный файл невозможно открыть как картинку",
                        "Файл имеет не поддерживаемое расширение или повреждён");
            }
        }
    }

    private boolean isValidImage(String absolutePath) {
        Path imgPath = Paths.get(absolutePath);
        if (Files.exists(imgPath)) {
            try (InputStream inputStream = Files.newInputStream(imgPath)) {
                Image bookImage = new Image(inputStream);
                return !bookImage.isError();

            } catch (IOException e) {
                LOGGER.error(format("Exception while load img %s", absolutePath), e);
            }
        }

        return false;
    }

    protected void addBook(TextField nameInput,
                           ComboBox<String> authorInput,
                           ComboBox<String> publisherInput,
                           TextField pagesCountInput,
                           CheckBox isReadCheckBox,
                           TextField bookShelfNumberInput,
                           ComboBox<String> cycleInput,
                           CheckBox isEndedCycleCheckBox,
                           TextField numberInCycleInput,
                           TextField totalCountInCycleInput,
                           TextField imageInput,
                           Button addButton) {

        String bookName = getInputText(nameInput);
        String author = getInputText(authorInput);
        String publisher = getInputText(publisherInput);
        String pages = getInputText(pagesCountInput);
        boolean isRead = isChecked(isReadCheckBox);

        String shelfNumber = getInputText(bookShelfNumberInput);

        String cycle = getInputText(cycleInput);
        boolean isEndedCycle = isChecked(isEndedCycleCheckBox);

        String numberInCycle = getInputText(numberInCycleInput);
        String totalInCycle = getInputText(totalCountInCycleInput);

        String imagePath = getInputText(imageInput);

        if (bookName == null) {
            riseAlert(WARNING, "Ошибка","Название книги не заполнено",
                    "Название книги не может быть пустым");
            return;
        }
        if (author == null) {
            riseAlert(WARNING, "Ошибка","Имя автора не заполнено",
                    "Имя автора не может быть пустым");
            return;
        }
        if (publisher == null) {
            riseAlert(WARNING, "Ошибка","Название издателя не заполнено",
                    "Название издателя не может быть пустым");
            return;
        }
        if (pages == null) {
            riseAlert(WARNING, "Ошибка","Количество страниц не заполнено",
                    "Количество страниц в книги не может быть пустым");
            return;
        }
        if (stringToInt(pages) == null) {
            riseAlert(WARNING, "Ошибка","Неверно заполнено количество страниц",
                    "Количество страниц должно быть записано в виде целого числа");
            return;
        }
        if (stringToInt(shelfNumber) == null) {
            riseAlert(WARNING, "Ошибка","Неверно заполнен номер полки",
                    "Номер полки должен быть записан в виде целого числа");
            return;
        }

        // Cycle validations
        if (isNotBlank(numberInCycle) && stringToInt(numberInCycle) == null) {
            riseAlert(WARNING, "Ошибка","Неверно заполнен номер книги в цикле",
                    "Номер книги в цикле должен быть записан в виде целого числа");
            return;
        }
        if (isNotBlank(totalInCycle) && stringToInt(totalInCycle) == null) {
            riseAlert(WARNING, "Ошибка","Неверно заполнено количество книги в цикле",
                    "Количество книг в цикле должно быть записано в виде целого числа");
            return;
        }
        if (isBlank(cycle) && isEndedCycle) {
            riseAlert(WARNING, "Ошибка","Название цикла не заполнено",
                    "Указано, что цикл окончен, но название цикла при этом не может быть пустым");
            return;
        }
        if (isBlank(cycle) && isNotBlank(numberInCycle)) {
            riseAlert(WARNING, "Ошибка","Название цикла не заполнено",
                    "Указан номер книги в цикле, но название цикла при этом не может быть пустым");
            return;
        }
        if (isBlank(cycle) && isNotBlank(totalInCycle)) {
            riseAlert(WARNING, "Ошибка","Название цикла не заполнено",
                    "Указано количество книг в цикле, но название цикла при этом не может быть пустым");
            return;
        }
        if (isNotBlank(cycle) && isBlank(numberInCycle)) {
            riseAlert(WARNING, "Ошибка","Неверно заполнен номер книги в цикле",
                    "Указано название цикла, но номер книги в цикле при этом не может быть пустым");
            return;
        }
        if (isNotBlank(cycle) && isBlank(totalInCycle)) {
            riseAlert(WARNING, "Ошибка","Неверно заполнено количество книги в цикле",
                    "Указано название цикла, но количество книг в цикле при этом не может быть пустым");
            return;
        }

        Book newBook = BookBuilder.builder()
                .name(bookName.trim())
                .author(author.trim())
                .publisher(publisher.trim())
                .pages(stringToInt(pages))
                .bookshelf(stringToInt(shelfNumber))
                .read(isRead)
                .cycleName(cycle != null ? cycle.trim() : null)
                .numberInSeries(stringToInt(numberInCycle))
                .coverImgAbsolutePath(imagePath)
                .build();

        Author newAuthor = new Author(author.trim());
        Publisher newPublisher = new Publisher(publisher.trim());
        Cycle newCycle = null;

        if (isNotBlank(cycle)) {
            newCycle = CycleBuilder.builder()
                    .name(cycle.trim())
                    .ended(isEndedCycle)
                    .booksInCycle(stringToInt(totalInCycle))
                    .build();
        }

        boolean isUpdated = doUpdate(newBook, newAuthor, newPublisher, newCycle);

        if (isUpdated) {
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();
        }
    }

    protected abstract boolean doUpdate(Book newBook, Author newAuthor, Publisher newPublisher, Cycle newCycle);

    private Integer stringToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getInputText(TextField textField) {
        String textFieldText = textField.getText();
        return isNotBlank(textFieldText) ? textFieldText : null;
    }

    private String getInputText(ComboBox<String> comboBox) {
        String comboBoxText = comboBox.getSelectionModel().getSelectedItem();
        return isNotBlank(comboBoxText) ? comboBoxText : null;
    }

    private boolean isChecked(CheckBox checkBox) {
        return checkBox.isSelected();
    }
}
