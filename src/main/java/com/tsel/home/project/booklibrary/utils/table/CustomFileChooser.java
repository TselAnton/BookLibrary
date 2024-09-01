package com.tsel.home.project.booklibrary.utils.table;

import static java.util.Optional.ofNullable;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public final class CustomFileChooser {

    private final FileChooser fileChooser;

    public CustomFileChooser(String title, File initialDirectory) {
        fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        ofNullable(initialDirectory).ifPresent(fileChooser::setInitialDirectory);
    }

    public void setInitialDirectory(File file) {
        fileChooser.setInitialDirectory(file);
    }

    public File showOpenDialog(Window window) {
        return fileChooser.showOpenDialog(window);
    }
}
