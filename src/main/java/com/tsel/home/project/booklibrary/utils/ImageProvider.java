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
            InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + "img/default.png");
            defaultImg = new Image(requireNonNull(imageInputStream));

        } catch (Exception e) {
            LOGGER.error("Exception while init BookInfoViewController", e);
        }
    }

    public Image getDefaultImage() {
        return this.defaultImg;
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
}
