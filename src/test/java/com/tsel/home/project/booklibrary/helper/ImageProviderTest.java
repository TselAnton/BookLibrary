package com.tsel.home.project.booklibrary.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.dao.data.Book;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageProviderTest extends UnitJavaFXTest {

    private static final String RED_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAASSURBVBhXY/jPwPAfhKEMhv8AR8oH+ax4QrwAAAAASUVORK5CYII=";
    private static final String BLUE_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAASSURBVBhXY2Bg+P8fgsHE//8AP9IH+WMJIRIAAAAASUVORK5CYII=";

    @Test
    void shouldReturnPlaceholderWithoutWaitingAndReplaceEveryBoundView(@TempDir Path directory) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch releaseLoader = blockLoader(executor);
        try (ImageProvider provider = new ImageProvider(executor)) {
            Book book = book(writeImage(directory, "обложка #1.png", RED_IMAGE));
            ImageProvider.ImageHolder holder = onFx(() -> provider.resolveSmallCover(book));
            ImageView first = onFx(() -> bind(holder));
            ImageView second = onFx(() -> bind(provider.resolveSmallCover(book)));
            CountDownLatch changed = new CountDownLatch(2);
            onFx(() -> {
                assertTrue(holder.isLoading());
                assertTrue(holder.isDefault());
                assertSame(first.getImage(), second.getImage());
                assertSame(holder, provider.resolveSmallCover(book));
                first.imageProperty().addListener((observable, previous, image) -> {
                    assertTrue(Platform.isFxApplicationThread());
                    changed.countDown();
                });
                second.imageProperty().addListener((observable, previous, image) -> changed.countDown());
                return null;
            });

            // The FX queue remains usable while the actual loader is deliberately blocked.
            assertTrue(onFx(Platform::isFxApplicationThread));
            releaseLoader.countDown();
            awaitLoaded(holder);
            assertTrue(changed.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                assertFalse(holder.isDefault());
                assertSame(holder.image(), first.getImage());
                assertSame(first.getImage(), second.getImage());
                assertEquals(Color.RED, first.getImage().getPixelReader().getColor(0, 0));
                assertSame(holder, provider.resolveSmallCover(book));
                return null;
            });
        } finally {
            releaseLoader.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldKeepPlaceholderForAbsentUnreadableAndInvalidFiles(@TempDir Path directory) throws Exception {
        try (ImageProvider provider = new ImageProvider()) {
            Path invalidImage = directory.resolve("broken.png");
            Files.writeString(invalidImage, "not an image");
            String[] paths = {null, "  ", directory.resolve("missing.png").toString(), invalidImage.toString(), "bad\u0000path"};
            for (String path : paths) {
                Book book = Book.builder().id(UUID.randomUUID()).coverImgAbsolutePath(path).build();
                ImageProvider.ImageHolder holder = onFx(() -> provider.resolveSmallCover(book));
                awaitLoaded(holder);
                assertTrue(onFx(holder::isDefault));
            }
        }
    }

    @Test
    void shouldDiscardOldRequestsWhenPathChangesOrCacheIsInvalidated(@TempDir Path directory) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch releaseLoader = blockLoader(executor);
        try (ImageProvider provider = new ImageProvider(executor)) {
            Path red = writeImage(directory, "red.png", RED_IMAGE);
            Path blue = writeImage(directory, "blue.png", BLUE_IMAGE);
            Book book = book(red);
            ImageProvider.ImageHolder old = onFx(() -> provider.resolveSmallCover(book));
            ImageView view = onFx(() -> bind(old));
            ImageProvider.ImageHolder replacement = onFx(() -> {
                book.setCoverImgAbsolutePath(blue.toString());
                ImageProvider.ImageHolder current = provider.resolveSmallCover(book);
                view.imageProperty().bind(current.imageProperty());
                return current;
            });
            assertNotSame(old, replacement);
            releaseLoader.countDown();
            awaitLoaded(replacement);
            onFx(() -> {
                assertTrue(old.isDefault());
                assertSame(replacement.image(), view.getImage());
                assertEquals(Color.BLUE, view.getImage().getPixelReader().getColor(0, 0));
                return null;
            });

            // Overwriting the same path must not retain the previous image after an edit.
            Files.copy(red, blue, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            ImageProvider.ImageHolder edited = onFx(() -> {
                provider.deleteImagesFromCache(book);
                return provider.resolveSmallCover(book);
            });
            assertNotSame(replacement, edited);
            awaitLoaded(edited);
            assertEquals(Color.RED, onFx(() -> edited.image().getPixelReader().getColor(0, 0)));

            ImageProvider.ImageHolder details = onFx(() -> provider.resolveCover(book));
            awaitLoaded(details);
            assertEquals(Color.RED, onFx(() -> details.image().getPixelReader().getColor(0, 0)));
            assertSame(details, onFx(() -> provider.resolveCover(book)));
            onFx(() -> {
                book.setCoverImgAbsolutePath(null);
                assertTrue(provider.resolveSmallCover(book).isDefault());
                assertTrue(provider.resolveCover(book).isDefault());
                return null;
            });
        } finally {
            releaseLoader.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldIgnoreOldCompletionAlreadyQueuedOnFxThread(@TempDir Path directory) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ImageProvider provider = new ImageProvider(executor)) {
            Book book = book(writeImage(directory, "red.png", RED_IMAGE));
            Path blue = writeImage(directory, "blue.png", BLUE_IMAGE);
            List<ImageProvider.ImageHolder> holders = onFx(() -> {
                ImageProvider.ImageHolder old = provider.resolveSmallCover(book);
                // Finish the worker while this FX action still keeps its UI callback queued.
                executor.submit(() -> {}).get(10, TimeUnit.SECONDS);
                book.setCoverImgAbsolutePath(blue.toString());
                ImageProvider.ImageHolder replacement = provider.resolveSmallCover(book);
                return List.of(old, replacement);
            });
            awaitLoaded(holders.get(1));
            assertTrue(onFx(holders.get(0)::isDefault), "An obsolete callback must not publish its image");
            assertEquals(Color.BLUE, onFx(() -> holders.get(1).image().getPixelReader().getColor(0, 0)));
        }
    }

    @Test
    void shouldCancelPendingLoadsOnClose(@TempDir Path directory) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch releaseLoader = blockLoader(executor);
        ImageProvider provider = new ImageProvider(executor);
        try {
            Book book = book(writeImage(directory, "red.png", RED_IMAGE));
            ImageProvider.ImageHolder holder = onFx(() -> provider.resolveSmallCover(book));
            provider.close();
            releaseLoader.countDown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            assertTrue(onFx(holder::isDefault));
        } finally {
            provider.close();
            releaseLoader.countDown();
        }
    }

    private static Book book(Path path) {
        return Book.builder().id(UUID.randomUUID()).coverImgAbsolutePath(path.toString()).build();
    }

    private static ImageView bind(ImageProvider.ImageHolder holder) {
        ImageView view = new ImageView();
        view.imageProperty().bind(holder.imageProperty());
        return view;
    }

    private static Path writeImage(Path directory, String name, String image) throws Exception {
        return Files.write(directory.resolve(name), Base64.getDecoder().decode(image));
    }

    private static CountDownLatch blockLoader(ExecutorService executor) {
        CountDownLatch release = new CountDownLatch(1);
        executor.submit(() -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return release;
    }

    private static void awaitLoaded(ImageProvider.ImageHolder holder) throws Exception {
        CountDownLatch complete = new CountDownLatch(1);
        onFx(() -> {
            if (!holder.isLoading()) {
                complete.countDown();
            } else {
                holder.loadingProperty().addListener((observable, previous, loading) -> {
                    if (!loading) {
                        complete.countDown();
                    }
                });
            }
            return null;
        });
        assertTrue(complete.await(10, TimeUnit.SECONDS), "The cover must finish loading");
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
