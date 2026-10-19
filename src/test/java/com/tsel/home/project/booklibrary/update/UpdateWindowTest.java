package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.ui.UpdateWindow;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class UpdateWindowTest extends UnitJavaFXTest {

    @Test
    void checkingIsAsynchronousAndNetworkFailureIsNotReportedAsUpToDate() throws Exception {
        CountDownLatch network = new CountDownLatch(1);
        AtomicReference<Stage> owner = new AtomicReference<>();
        AtomicReference<Stage> update = new AtomicReference<>();
        try {
            onFx(() -> {
                Stage main = new Stage();
                main.setScene(new Scene(new VBox(), 812, 600));
                main.show();
                owner.set(main);
                update.set(new UpdateWindow(main, new GitHubReleaseClient(new StubTransport(network, false))).show());
                assertTrue(update.get().isShowing());
                assertTrue(((Label) update.get().getScene().lookup("#updateStatus")).getText().contains("Проверяем"));
                assertTrue(update.get().getScene().lookup("#updateActionButton").isDisabled());
            });
            network.countDown();
            awaitStatus(update, "Новых стабильных версий нет");
            onFx(() -> {
                Stage stage = update.get();
                Button action = (Button) stage.getScene().lookup("#updateActionButton");
                assertFalse(action.isDisabled());
                javafx.geometry.Bounds bounds = action.localToScene(action.getBoundsInLocal());
                assertTrue(bounds.getMaxY() <= stage.getScene().getHeight());
                stage.close();
                update.set(new UpdateWindow(owner.get(), new GitHubReleaseClient(new StubTransport(network, true))).show());
            });
            awaitStatus(update, "Тестовое отсутствие сети");
            onFx(() -> {
                assertTrue(update.get().isShowing());
                assertFalse(update.get().getScene().lookup("#updateActionButton").isDisabled());
            });
        } finally {
            network.countDown();
            onFx(() -> {
                if (update.get() != null) {
                    update.get().hide();
                }
                if (owner.get() != null) {
                    owner.get().hide();
                }
            });
        }
    }

    private void awaitStatus(AtomicReference<Stage> window, String expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            FutureTask<Boolean> state = new FutureTask<>(() -> ((Label) window.get().getScene().lookup("#updateStatus")).getText().contains(expected));
            Platform.runLater(state);
            if (state.get(5, TimeUnit.SECONDS)) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Missing update state: " + expected);
    }

    private void onFx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private record StubTransport(CountDownLatch network, boolean fail) implements ReleaseTransport {

        @Override
        public byte[] read(URI uri, long limit) throws IOException, InterruptedException {
            if (!network.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Network gate timed out");
            }
            if (fail) {
                throw new IOException("Тестовое отсутствие сети");
            }
            return "{\"draft\":false,\"prerelease\":false,\"tag_name\":\"v4.1.0\"}".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void download(URI uri, Path target, long expectedSize, DoubleConsumer progress) {
            throw new AssertionError("No download expected");
        }
    }
}
