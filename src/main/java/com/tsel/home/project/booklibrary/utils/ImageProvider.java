package com.tsel.home.project.booklibrary.utils;

import static com.tsel.home.project.booklibrary.controller.AbstractViewController.RESOURCE_PATH;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.tsel.home.project.booklibrary.controller.BookInfoViewController;
import com.tsel.home.project.booklibrary.dao.data.Book;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageProvider {

    private static final Logger LOGGER = LogManager.getLogger(BookInfoViewController.class);

    private static ImageProvider INSTANCE;

    private final Map<String, Image> cashedImages = new HashMap<>();
    private final Map<String, Image> cashedSmallImages = new HashMap<>();
    private Image defaultImg;

    public static ImageProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageProvider();
        }
        return INSTANCE;
    }

    private ImageProvider() {
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
            Image cashedImage = cashedImages.get(book.getKey());
            if (cashedImage != null) {
                return cashedImage;
            }

            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try (InputStream inputStream = Files.newInputStream(imgPath)) {
                    Image bookImage = new Image(inputStream);
                    if (!bookImage.isError()) {
                        cashedImages.put(book.getKey().toLowerCase(Locale.ROOT), bookImage);
                        return bookImage;
                    }
                    return defaultImg;

                } catch (IOException e) {
                    LOGGER.error(format("Exception while load img %s", book.getCoverImgAbsolutePath()), e);
                }
            }
        }

        return defaultImg;
    }

    public Image resolveSmallCover(Book book) {
        if (isNotBlank(book.getCoverImgAbsolutePath())) {
            Image cashedImage = cashedSmallImages.get(book.getKey());
            if (cashedImage != null) {
                return cashedImage;
            }

            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try {
                    Image bookImage = new Image(imgPath.toUri().toURL().toExternalForm(), 12, 0, true, true, true);
                    if (!bookImage.isError()) {
                        cashedSmallImages.put(book.getKey().toLowerCase(Locale.ROOT), bookImage);
                        return bookImage;
                    }
                } catch (MalformedURLException e) {
                    LOGGER.error(format("Exception while load img %s", book.getCoverImgAbsolutePath()), e);
                }
            }
        }

        return defaultImg;
    }

    public void deleteImagesFromCache(Book book) {
        cashedImages.remove(book.getKey().toLowerCase(Locale.ROOT));
        cashedSmallImages.remove(book.getKey().toLowerCase(Locale.ROOT));
    }

    private Image loadImage(String path) {
        InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + path);
        return new Image(requireNonNull(imageInputStream));
    }
}
