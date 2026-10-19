package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.UnitJavaFXTest;
import com.tsel.home.project.booklibrary.ui.StartupUpdateCheck;
import com.tsel.home.project.booklibrary.ui.UpdateWindow;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

class StartupUpdateCheckTest extends UnitJavaFXTest {

    @Test
    void offersNewReleaseWithoutBlockingStartupOrCheckingAgain() throws Exception {
        StubTransport transport = new StubTransport("6.0.0", false);
        AtomicReference<Task<?>> task = new AtomicReference<>();
        AtomicReference<Stage> owner = new AtomicReference<>();
        try {
            onFx(() -> {
                owner.set(mainWindow());
                task.set(StartupUpdateCheck.start(owner.get(), new GitHubReleaseClient(transport)));
                assertEquals(1, Window.getWindows().size());
                assertTrue(owner.get().isShowing());
            });
            transport.network.countDown();
            awaitCheck(task.get());
            onFx(() -> {
                assertEquals(2, Window.getWindows().size());
                Window update = Window.getWindows().stream().filter(window -> window != owner.get()).findFirst().orElseThrow();
                assertTrue(((Label) update.getScene().lookup("#updateStatus")).getText().contains("6.0.0"));
                assertEquals(2, transport.reads.get(), "Use the checked release without another GitHub request");
            });
        } finally {
            transport.network.countDown();
            closeWindows();
        }
    }

    @Test
    void noNewReleaseAndOfflineStartupDoNotOpenDialogs() throws Exception {
        for (boolean offline : new boolean[] {false, true}) {
            StubTransport transport = new StubTransport("4.1.0", offline);
            AtomicReference<Task<?>> task = new AtomicReference<>();
            try {
                onFx(() -> task.set(StartupUpdateCheck.start(mainWindow(), new GitHubReleaseClient(transport))));
                transport.network.countDown();
                awaitCheck(task.get());
                onFx(() -> {
                    assertEquals(1, Window.getWindows().size());
                    assertTrue(Window.getWindows().get(0).isShowing());
                    assertEquals(offline ? Worker.State.FAILED : Worker.State.SUCCEEDED, task.get().getState());
                });
            } finally {
                transport.network.countDown();
                closeWindows();
            }
        }
    }

    @Test
    void closingMainWindowCancelsCheckAndManualDialogPreventsDuplicate() throws Exception {
        StubTransport transport = new StubTransport("6.0.0", false);
        AtomicReference<Task<?>> task = new AtomicReference<>();
        try {
            onFx(() -> {
                Stage owner = mainWindow();
                task.set(StartupUpdateCheck.start(owner, new GitHubReleaseClient(transport)));
                owner.hide();
            });
            awaitCheck(task.get());
            onFx(() -> assertTrue(task.get().isCancelled()));
            transport.network.countDown();
            onFx(() -> {
                Stage owner = mainWindow();
                new UpdateWindow(owner, new GitHubReleaseClient(transport)).show();
                task.set(StartupUpdateCheck.start(owner, new GitHubReleaseClient(transport)));
            });
            awaitCheck(task.get());
            onFx(() -> {
                assertEquals(2, Window.getWindows().size());
                assertFalse(task.get().isCancelled());
            });
        } finally {
            transport.network.countDown();
            closeWindows();
        }
    }

    private Stage mainWindow() {
        Stage stage = new Stage();
        stage.setScene(new Scene(new VBox(), 812, 600));
        stage.show();
        return stage;
    }

    private void awaitCheck(Task<?> task) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            FutureTask<Boolean> terminal = new FutureTask<>(() -> task.getState() == Worker.State.SUCCEEDED
                || task.getState() == Worker.State.FAILED || task.getState() == Worker.State.CANCELLED);
            Platform.runLater(terminal);
            if (terminal.get(5, TimeUnit.SECONDS)) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Startup check did not complete");
    }

    private void closeWindows() throws Exception {
        onFx(() -> new ArrayList<>(Window.getWindows()).forEach(Window::hide));
    }

    private void onFx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static class StubTransport implements ReleaseTransport {

        private final CountDownLatch network = new CountDownLatch(1);
        private final AtomicInteger reads = new AtomicInteger();
        private final String version;
        private final boolean offline;

        private StubTransport(String version, boolean offline) {
            this.version = version;
            this.offline = offline;
        }

        @Override
        public byte[] read(URI uri, long limit) throws IOException, InterruptedException {
            reads.incrementAndGet();
            if (!network.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Network gate timed out");
            }
            if (offline) {
                throw new IOException("Test offline startup");
            }
            String base = GitHubReleaseClient.REPOSITORY_URL + "/releases/download/v" + version + "/";
            String jar = "BookLibraryApp-" + UpdateFixtures.PLATFORM + ".jar";
            String metadata = "update-" + UpdateFixtures.PLATFORM + ".properties";
            String body = uri.getHost().equals("api.github.com")
                ? "{\"draft\":false,\"prerelease\":false,\"tag_name\":\"v" + version + "\",\"body\":\"Changes\",\"assets\":["
                    + asset(metadata, base) + "," + asset(jar, base) + "]}"
                : "protocol=1\nversion=" + version + "\nplatform=" + UpdateFixtures.PLATFORM + "\njava.minimum=17\nasset=" + jar
                    + "\nsha256=" + "0".repeat(64) + "\n";
            return body.getBytes(StandardCharsets.UTF_8);
        }

        private String asset(String name, String base) {
            return "{\"name\":\"" + name + "\",\"state\":\"uploaded\",\"size\":123,\"browser_download_url\":\"" + base + name + "\"}";
        }

        @Override
        public void download(URI uri, Path target, long expectedSize, DoubleConsumer progress) {
            throw new AssertionError("Startup must not download or install automatically");
        }
    }
}
