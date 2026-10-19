package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdaterProcessTest {

    @Test
    void standaloneHelperWaitsForRealProcessExitAndReplacesJar(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan initial = UpdatePlan.read(file);
        Path helper = directory.resolve("отдельный помощник.jar");
        try (InputStream resource = getClass().getResourceAsStream(AppVersion.helperResource())) {
            assertTrue(resource != null, "Build must embed a standalone helper before tests");
            Files.copy(resource, helper);
        }
        try (JarFile jar = new JarFile(helper.toFile())) {
            assertFalse(jar.stream().anyMatch(entry -> entry.getName().startsWith("javafx/") || entry.getName().startsWith("com/google/")));
        }
        Path ownerReady = directory.resolve("owner-ready");
        Path ownerStop = directory.resolve("owner-stop");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = Path.of("target/test-classes").toAbsolutePath() + File.pathSeparator + Path.of("target/classes").toAbsolutePath();
        Process owner = new ProcessBuilder(java, "-cp", classpath, UpdateProcessFixture.class.getName(), initial.dataDirectory().toString(),
            ownerReady.toString(), ownerStop.toString()).redirectErrorStream(true).redirectOutput(directory.resolve("owner.log").toFile()).start();
        Process installer = null;
        try {
            awaitFile(ownerReady);
            assertThrows(IOException.class, () -> InstallationLock.acquire(initial.dataDirectory()));
            UpdatePlan plan = new UpdatePlan(initial.target(), initial.source(), initial.dataDirectory(), initial.backup(), initial.version(), initial.platform(),
                initial.sha256(), initial.originalSha256(), owner.pid(), owner.info().startInstant().orElseThrow().toEpochMilli());
            plan.write(file);
            installer = new ProcessBuilder(java, "-jar", helper.toString(), file.toString(), "--headless")
                .redirectErrorStream(true).redirectOutput(directory.resolve("helper.log").toFile()).start();
            awaitFile(file.resolveSibling("ready.properties"));
            assertTrue(installer.isAlive());
            assertTrue(owner.isAlive(), "Helper must not kill the app");
            assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
            Files.writeString(ownerStop, "stop");
            assertTrue(owner.waitFor(10, TimeUnit.SECONDS));
            assertTrue(installer.waitFor(15, TimeUnit.SECONDS));
            assertEquals(0, installer.exitValue(), Files.readString(directory.resolve("helper.log")));
            assertEquals(plan.sha256(), UpdateFiles.sha256(plan.target()));
            assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.backup().resolve("application.jar")));
            assertFalse(Files.exists(plan.dataDirectory().resolve(UpdateFiles.PENDING)));
        } finally {
            owner.destroyForcibly();
            if (installer != null) {
                installer.destroyForcibly();
            }
        }
    }

    private void awaitFile(Path path) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!Files.exists(path) && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(Files.exists(path), "Missing handshake: " + path);
    }
}
