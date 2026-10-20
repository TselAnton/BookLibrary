package com.tsel.home.project.booklibrary.update.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.update.UpdateFixtures;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateInstallerTest {

    @Test
    void verifiedBackupPrecedesReplacementAndDataStayUntouched(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        String json = Files.readString(plan.dataDirectory().resolve("repository/nested/custom.json"));
        UpdateInstaller installer = new UpdateInstaller();
        installer.prepare(file);
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
        assertTrue(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
        installer.install(file);
        assertEquals(plan.sha256(), UpdateFiles.sha256(plan.target()));
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.backup().resolve("application.jar")));
        assertEquals(json, Files.readString(plan.backup().resolve("data/repository/nested/custom.json")));
        assertEquals(json, Files.readString(plan.dataDirectory().resolve("repository/nested/custom.json")));
        assertEquals("maxArchivedBackups=0\n", Files.readString(plan.backup().resolve("data/.properties")));
        assertEquals(plan.dataDirectory().toString(), UpdateFiles.readProperties(plan.installation().resolve(UpdateFiles.DATA_LOCATION)).getProperty("dataDirectory"));
        assertFalse(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
        assertFalse(Files.exists(plan.dataDirectory().resolve(UpdateFiles.PENDING)));
        assertTrue(Files.exists(plan.backup().resolve("complete.properties")));
    }

    @Test
    void failedReplacementKeepsOriginalAndVerifiedBackup(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        UpdateInstaller installer = new UpdateInstaller((source, target) -> { throw new IOException("Access denied"); });
        installer.prepare(file);
        IOException failure = assertThrows(IOException.class, () -> installer.install(file));
        installer.failed(file, failure);
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.backup().resolve("application.jar")));
        assertFalse(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
        assertEquals("failed", UpdateFiles.readProperties(plan.dataDirectory().resolve(".booklibrary-update-result.properties")).getProperty("status"));
    }

    @Test
    void interruptionAfterReplacementCanResumeWithoutDownloadedFile(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        UpdateInstaller interrupted = new UpdateInstaller((source, target) -> {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            throw new IOException("Simulated interruption after replacement");
        });
        interrupted.prepare(file);
        IOException failure = assertThrows(IOException.class, () -> interrupted.install(file));
        interrupted.failed(file, failure);
        assertTrue(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
        Files.delete(plan.source());
        new UpdateInstaller().install(file);
        assertEquals(plan.sha256(), UpdateFiles.sha256(plan.target()));
        assertFalse(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
    }

    @Test
    void corruptDownloadOrFailedBackupNeverChangesApplication(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        Files.writeString(plan.source(), "truncated download");
        UpdateInstaller installer = new UpdateInstaller();
        assertThrows(IOException.class, () -> installer.prepare(file));
        assertFalse(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
        UpdateFixtures.jar(plan.source(), "6.0.0");
        // ZIP entry timestamps can change; preserve the original plan hash explicitly.
        UpdatePlan repaired = new UpdatePlan(plan.target(), plan.source(), plan.dataDirectory(), plan.backup(), plan.version(), plan.platform(),
            UpdateFiles.sha256(plan.source()), plan.originalSha256(), plan.processId(), plan.processStarted());
        repaired.write(file);
        Files.createDirectories(plan.backup());
        Files.createFile(plan.backup().resolve("data"));
        installer.prepare(file);
        assertThrows(IOException.class, () -> installer.install(file));
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
    }

    @Test
    void modifiedBackupPreventsResumingAnInterruptedUpdate(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        UpdateInstaller installer = new UpdateInstaller((source, target) -> { throw new IOException("Interrupted"); });
        installer.prepare(file);
        assertThrows(IOException.class, () -> installer.install(file));
        Files.writeString(plan.backup().resolve("data/repository/nested/custom.json"), "corrupted backup");
        assertThrows(IOException.class, () -> new UpdateInstaller().install(file));
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
    }

    @Test
    void damagedTargetIsRestoredFromVerifiedBackup(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        UpdateInstaller installer = new UpdateInstaller((source, target) -> {
            Files.writeString(target, "simulated damaged replacement");
            throw new IOException("Simulated storage failure");
        });
        installer.prepare(file);
        IOException failure = assertThrows(IOException.class, () -> installer.install(file));
        installer.failed(file, failure);
        assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
        assertFalse(Files.exists(plan.installation().resolve(UpdateFiles.PENDING)));
    }

    @Test
    void failureBeforeOwnerExitNeverRestoresOrReplacesJar(@TempDir Path directory) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan plan = UpdatePlan.read(file);
        Files.writeString(plan.target(), "externally changed while app is running");
        String changedHash = UpdateFiles.sha256(plan.target());
        assertThrows(IOException.class, () -> new UpdateInstaller().failedBeforeInstall(file, new IOException("Owner is still running")));
        assertEquals(changedHash, UpdateFiles.sha256(plan.target()));
    }
}
