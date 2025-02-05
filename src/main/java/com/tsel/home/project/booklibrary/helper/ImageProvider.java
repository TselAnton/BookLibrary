package com.tsel.home.project.booklibrary.helper;

import static com.tsel.home.project.booklibrary.controller.AbstractViewController.RESOURCE_PATH;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.tsel.home.project.booklibrary.dao.data.Book;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ImageProvider {

    private final Map<UUID, Image> cashedImages = new ConcurrentHashMap<>();
    private final Map<UUID, Image> cashedSmallImages = new ConcurrentHashMap<>();
    private final Image cachedIcon;
    private final Image defaultImg;

    public ImageProvider() {
        try {
            this.cachedIcon = loadImage("img/icon.png");
            this.defaultImg = loadImage("img/default.png");
        } catch (Exception ex) {
            log.error("Exception while init abstract constructor", ex);
            throw ex;
        }
    }

    public Image getWindowIcon() {
        return cachedIcon;
    }

    public Image resolveCover(Book book) {
        if (isNotBlank(book.getCoverImgAbsolutePath())) {
            Image cashedImage = cashedImages.get(book.getId());
            if (cashedImage != null) {
                return cashedImage;
            }

            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try (InputStream inputStream = Files.newInputStream(imgPath)) {
                    Image bookImage = new Image(inputStream);
                    if (!bookImage.isError()) {
                        cashedImages.put(book.getId(), bookImage);
                        return bookImage;
                    }
                    return defaultImg;

                } catch (IOException e) {
                    log.error(format("Exception while load img %s", book.getCoverImgAbsolutePath()), e);
                }
            }
        }

        return defaultImg;
    }

    public ImageHolder resolveSmallCover(Book book) {
        if (isNotBlank(book.getCoverImgAbsolutePath())) {
            Image cashedImage = cashedSmallImages.get(book.getId());
            if (cashedImage != null) {
                return new ImageHolder(cashedImage, false);
            }

            Path imgPath = Paths.get(book.getCoverImgAbsolutePath());
            if (Files.exists(imgPath)) {
                try {
                    Image bookImage = new Image(imgPath.toUri().toURL().toExternalForm(), 12, 0, true, true, true);
                    if (!bookImage.isError()) {
                        cashedSmallImages.put(book.getId(), bookImage);
                        return new ImageHolder(bookImage, false);
                    }
                } catch (MalformedURLException e) {
                    log.error(format("Exception while load img %s", book.getCoverImgAbsolutePath()), e);
                }
            }
        }

        return new ImageHolder(defaultImg, true);
    }

    public void deleteImagesFromCache(Book book) {
        cashedImages.remove(book.getId());
        cashedSmallImages.remove(book.getId());
    }

    private Image loadImage(String path) {
        try (InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + path)) {
            return new Image(requireNonNull(imageInputStream));
        } catch (IOException e) {
            throw new IllegalStateException(format("Exception while loading image by path '%s'", path), e);
        }
    }

    public record ImageHolder(Image image, boolean isDefault) {}
}
