package com.tsel.home.project.booklibrary.utils;

import static com.tsel.home.project.booklibrary.controller.AbstractViewController.RESOURCE_PATH;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.tsel.home.project.booklibrary.controller.BookInfoViewController;
import com.tsel.home.project.booklibrary.data.Book;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageProvider {

    private static final Logger LOGGER = LogManager.getLogger(BookInfoViewController.class);

    private Image defaultImg;

    public ImageProvider() {
        try {
            this.defaultImg = loadImage("img/default.png");
        } catch (Exception e) {
            LOGGER.error("Exception while init abstract constructor", e);
        }
    }

    public Image loadIcon() {
        try {
            return loadImage("img/icon.png");
        } catch (Exception e) {
            LOGGER.error("Exception while load icon", e);
            return null;
        }
    }

    public Image resolveCover(Book book) {
        if (isNotBlank(book.getCoverImgAbsolutePath())) {
            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try (InputStream inputStream = Files.newInputStream(imgPath)) {
                    Image bookImage = new Image(inputStream);
                    return bookImage.isError()
                        ? defaultImg
                        : bookImage;

                } catch (IOException e) {
                    LOGGER.error(format("Exception while load img %s", book.getCoverImgAbsolutePath()), e);
                }
            }
        }

        return defaultImg;
    }

    private Image loadImage(String path) {
        InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + path);
        return new Image(requireNonNull(imageInputStream));
    }
}
