package com.tsel.home.project.booklibrary.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.helper.DataExporter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataExporterTest {

    @Test
    void exportsAllJsonIncludingUnknownAndNestedFiles(@TempDir Path directory) throws Exception {
        Path repository = Files.createDirectory(directory.resolve("repository"));
        Files.createDirectories(repository.resolve("nested"));
        Files.writeString(repository.resolve("authors.json"), "[{\"name\":\"Антон\"}]", StandardCharsets.UTF_8);
        Files.writeString(repository.resolve("nested/custom.JSON"), "{\"extra\":true}", StandardCharsets.UTF_8);
        Files.writeString(repository.resolve("notes.txt"), "не экспортировать");
        DataExporter exporter = new DataExporter();
        Path archive = exporter.export(repository, directory);
        Path second = exporter.export(repository, directory);
        assertTrue(Files.exists(archive));
        assertTrue(!archive.equals(second));

        Map<String, String> contents = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                contents.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        assertEquals(Map.of("authors.json", "[{\"name\":\"Антон\"}]", "nested/custom.JSON", "{\"extra\":true}"), contents);
    }

    @Test
    void missingOrEmptyRepositoryDoesNotLeaveArchive(@TempDir Path directory) throws Exception {
        DataExporter exporter = new DataExporter();
        assertThrows(IOException.class, () -> exporter.export(directory.resolve("missing"), directory));
        assertThrows(IOException.class, () -> exporter.export(directory, directory));
        try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
            assertEquals(0, paths.count());
        }
    }
}
