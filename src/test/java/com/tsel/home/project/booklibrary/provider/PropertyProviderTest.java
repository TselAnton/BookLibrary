package com.tsel.home.project.booklibrary.provider;

import static com.tsel.home.project.booklibrary.TestFileUtils.readFileContent;
import static com.tsel.home.project.booklibrary.TestFileUtils.writeFileContent;
import static com.tsel.home.project.booklibrary.helper.PropertyProvider.MAX_ARCHIVED_BACKUPS;
import static com.tsel.home.project.booklibrary.helper.PropertyProvider.PROPERTY_FILE_PATH;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tsel.home.project.booklibrary.helper.PropertyProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PropertyProviderTest {

    @Test
    void createPropertyProviderShouldInitPropertyFile(@TempDir Path tempDir) {
        Path propertyFilePath = tempDir.resolve(PROPERTY_FILE_PATH);

        // Проверка, что файл не существует
        assertFalse(Files.exists(propertyFilePath));

        // Создаём провайдер
        new PropertyProvider(tempDir);

        // Проверка, что файл создался
        assertTrue(Files.exists(propertyFilePath));

        // Проверка содержимого
        String propertyFileContent = readFileContent(propertyFilePath);
        assertNotNull(propertyFileContent);
        assertTrue(propertyFileContent.contains(MAX_ARCHIVED_BACKUPS + "=5"));
    }

    @Test
    void createPropertyProviderShouldNotOverwriteExitedValues(@TempDir Path tempDir) {
        Path propertyFilePath = tempDir.resolve(PROPERTY_FILE_PATH);

        // Записываем в property файл значение
        writeFileContent(MAX_ARCHIVED_BACKUPS + "=33", propertyFilePath);
        assertTrue(Files.exists(propertyFilePath));

        // Создаём провайдер
        PropertyProvider propertyProvider = new PropertyProvider(tempDir);
        assertEquals("33", propertyProvider.getProperty(MAX_ARCHIVED_BACKUPS));
    }

    @Test
    void getPropertyShouldReturnDefaultPropertyWhenItNotExists(@TempDir Path tempDir) {
        Path propertyFilePath = tempDir.resolve(PROPERTY_FILE_PATH);

        // Записываем в property файл значение
        writeFileContent( "incorrectProperty=33", propertyFilePath);
        assertTrue(Files.exists(propertyFilePath));

        // Создаём провайдер
        PropertyProvider propertyProvider = new PropertyProvider(tempDir);
        assertEquals("5", propertyProvider.getProperty(MAX_ARCHIVED_BACKUPS));
    }
}
