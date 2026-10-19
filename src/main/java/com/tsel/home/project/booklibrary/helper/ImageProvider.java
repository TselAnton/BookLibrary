package com.tsel.home.project.booklibrary.helper;

import static com.tsel.home.project.booklibrary.controller.AbstractViewController.RESOURCE_PATH;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.tsel.home.project.booklibrary.dao.data.Book;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ImageProvider implements AutoCloseable {

    private final Map<UUID, ImageHolder> cachedImages = new ConcurrentHashMap<>();
    private final Map<UUID, ImageHolder> cachedSmallImages = new ConcurrentHashMap<>();
    private final ExecutorService coverExecutor;
    private final Image cachedIcon;
    private final Image defaultImg;
    private final ImageHolder defaultCover;

    public ImageProvider() {
        this(Executors.newFixedThreadPool(2, action -> {
            Thread thread = new Thread(action, "book-cover-loader");
            thread.setDaemon(true);
            return thread;
        }));
    }

    ImageProvider(ExecutorService coverExecutor) {
        this.coverExecutor = coverExecutor;
        try {
            this.cachedIcon = loadImage("img/icon.png", 0, 0);
            this.defaultImg = loadImage("img/default-book-cover.png", 244, 370);
            this.defaultCover = new ImageHolder(null);
        } catch (Exception ex) {
            coverExecutor.shutdownNow();
            log.error("Exception while init abstract constructor", ex);
            throw ex;
        }
    }

    public Image getWindowIcon() {
        return javafx.application.Platform.isFxApplicationThread()
            ? com.tsel.home.project.booklibrary.ui.SageTheme.windowIcon() : cachedIcon;
    }

    public boolean isPlaceholder(Image image) {
        return image == defaultImg;
    }

    public ImageHolder resolveCover(Book book) {
        return resolveCover(book, cachedImages, 488, 740);
    }

    public ImageHolder resolveSmallCover(Book book) {
        return resolveCover(book, cachedSmallImages, 24, 32);
    }

    public void deleteImagesFromCache(Book book) {
        cancel(cachedImages.remove(book.getId()));
        cancel(cachedSmallImages.remove(book.getId()));
    }

    @Override
    public void close() {
        cachedImages.values().forEach(this::cancel);
        cachedSmallImages.values().forEach(this::cancel);
        coverExecutor.shutdownNow();
        cachedImages.clear();
        cachedSmallImages.clear();
    }

    private ImageHolder resolveCover(Book book, Map<UUID, ImageHolder> cache, double width, double height) {
        String sourcePath = book.getCoverImgAbsolutePath();
        if (!isNotBlank(sourcePath)) {
            cancel(cache.remove(book.getId()));
            return defaultCover;
        }

        return cache.compute(book.getId(), (id, previous) -> {
            if (previous != null && Objects.equals(previous.sourcePath, sourcePath)) {
                return previous;
            }

            cancel(previous);
            ImageHolder holder = new ImageHolder(sourcePath);
            holder.loadingTask = coverExecutor.submit(() -> {
                Image image = loadCover(sourcePath, width, height);
                if (!Thread.currentThread().isInterrupted() && !holder.cancelled) {
                    Platform.runLater(() -> {
                        if (!holder.cancelled) {
                            holder.image.set(image);
                            holder.loading.set(false);
                        }
                    });
                }
            });
            return holder;
        });
    }

    private Image loadCover(String sourcePath, double width, double height) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(sourcePath))) {
            Image image = new Image(inputStream, width, height, true, true);
            return image.isError() ? defaultImg : image;
        } catch (IOException | IllegalArgumentException e) {
            log.debug("Can't load book cover '{}'", sourcePath, e);
            return defaultImg;
        }
    }

    private void cancel(ImageHolder holder) {
        if (holder != null) {
            holder.cancelled = true;
            if (holder.loadingTask != null) {
                holder.loadingTask.cancel(true);
            }
        }
    }

    private Image loadImage(String path, double width, double height) {
        try (InputStream imageInputStream = this.getClass().getResourceAsStream(RESOURCE_PATH + path)) {
            return new Image(requireNonNull(imageInputStream), width, height, true, true);
        } catch (IOException e) {
            throw new IllegalStateException(format("Exception while loading image by path '%s'", path), e);
        }
    }

    public final class ImageHolder {

        private final String sourcePath;
        private final ReadOnlyObjectWrapper<Image> image = new ReadOnlyObjectWrapper<>(defaultImg);
        private final ReadOnlyBooleanWrapper loading;

        private volatile boolean cancelled;
        private Future<?> loadingTask;

        private ImageHolder(String sourcePath) {
            this.sourcePath = sourcePath;
            this.loading = new ReadOnlyBooleanWrapper(sourcePath != null);
        }

        public Image image() {
            return image.get();
        }

        public ReadOnlyObjectProperty<Image> imageProperty() {
            return image.getReadOnlyProperty();
        }

        public boolean isDefault() {
            return isPlaceholder(image());
        }

        public boolean isLoading() {
            return loading.get();
        }

        public ReadOnlyBooleanProperty loadingProperty() {
            return loading.getReadOnlyProperty();
        }
    }
}
