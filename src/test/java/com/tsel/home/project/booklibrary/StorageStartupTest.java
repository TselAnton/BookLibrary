package com.tsel.home.project.booklibrary;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.helper.DateProvider;
import com.tsel.home.project.booklibrary.helper.SimpleApplicationContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageStartupTest {

    @Test
    void coldStartupCreatesBackupBeforeMigration(@TempDir Path directory) throws Exception {
        DateProvider previous = SimpleApplicationContext.getBean(DateProvider.class);
        SimpleApplicationContext.replaceBean(DateProvider.class, () -> null);
        Path repository = Files.createDirectory(directory.resolve("repository"));
        Path backup = directory.resolve("backup");
        String original = "[{}]";
        Files.writeString(repository.resolve("userSettingsStorage.json"), original);
        try {
            App.JavaFXRunner.initializeStorage(repository, backup);
            assertNotNull(SimpleApplicationContext.getBean(DateProvider.class));
            assertTrue(Files.readString(repository.resolve("userSettingsStorage.json")).contains("appliedSchemaVersions"));
            try (java.util.stream.Stream<Path> archives = Files.list(backup);
                ZipFile archive = new ZipFile(archives.findFirst().orElseThrow().toFile())) {
                try (java.io.InputStream input = archive.getInputStream(archive.getEntry("userSettingsStorage.json"))) {
                    assertEquals(original, new String(input.readAllBytes(), UTF_8));
                }
            }
        } finally {
            SimpleApplicationContext.replaceBean(DateProvider.class, () -> previous);
        }
    }
}
