package com.tsel.home.project.booklibrary.utils;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FileUtils {

    private FileUtils() {}

    /**
     * @param pathParts Дополнительные части пути
     * @return {@link Path} до файла из текущей директории проекта
     */
    public static Path buildPathFromCurrentDir(String... pathParts) {
        return Paths.get(
            FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString(),
            pathParts
        );
    }

    /**
     * Создание файла по переданному пути
     * @param filePath {@link Path} файла
     */
    public static void createFile(Path filePath) {
        log.info("Starting to create file by path '{}'", filePath);

        try {
            log.info("Trying to create parent directory '{}'", filePath.getParent());
            Files.createDirectories(filePath.getParent());

        } catch (FileAlreadyExistsException ex) {
            log.info("Parent directory '{}' already exists. Skip directory creation", filePath.getParent());

        } catch (IOException ex) {
            throw new IllegalStateException(
                format("Проблема при создании файла '%s'. Невозможно создать родительскую директорию '%s'", filePath, filePath.getParent()),
                ex
            );
        }

        try {
            log.info("Trying to create file '{}'", filePath);
            Files.createFile(filePath);

        } catch (FileAlreadyExistsException ex) {
            log.info("File '{}' already exists. Skip file creation", filePath);

        } catch (IOException ex) {
            log.error("Exception while trying to create file '{}'", filePath);
            throw new IllegalStateException(format("Проблема при создании файла '%s'", filePath), ex);
        }
    }

    /**
     * @param storagePath {@link Path} до файла
     * @return true, если файл существует, иначе false
     */
    public static boolean isNotExists(Path storagePath) {
        return !Files.exists(storagePath);
    }
}
