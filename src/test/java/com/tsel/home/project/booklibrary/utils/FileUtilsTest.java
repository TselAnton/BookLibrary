package com.tsel.home.project.booklibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

    @Test
    void testBuildPathFromCurrentDir() {
        assertEquals(System.getProperty("user.dir") + "\\test1", FileUtils.buildPathFromCurrentDir("", "test1").toString());
        assertEquals(System.getProperty("user.dir") + "\\test2", FileUtils.buildPathFromCurrentDir("", "/test2").toString());
    }

    @Test
    void testCreateFile(@TempDir Path tempDir) {
        Path filePath = tempDir.resolve("/test1/test2/test3-%s.txt".formatted(UUID.randomUUID()));
        assertFalse(Files.exists(filePath));
        assertTrue(FileUtils.isNotExists(filePath));

        FileUtils.createFile(filePath);
        assertTrue(Files.exists(filePath));
        assertFalse(FileUtils.isNotExists(filePath));
    }
}