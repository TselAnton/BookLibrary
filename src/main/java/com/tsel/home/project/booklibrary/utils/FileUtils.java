package com.tsel.home.project.booklibrary.utils;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * Зарезолвить путь вместе с родительским
     * @param rootPath Родительский путь
     * @param path Основной путь к файлу
     * @return Объеденённый путь
     */
    public static Path resolvePath(Path rootPath, Path path) {
        return rootPath != null
            ? Path.of(rootPath.toAbsolutePath().toString(), path.toString())
            : path;
    }

    /**
     * Создание файла по переданному пути
     * @param filePath {@link Path} файла
     */
    public static void createFile(Path filePath) {

        try {
            Files.createDirectories(filePath.getParent());

        } catch (FileAlreadyExistsException ex) {

        } catch (IOException ex) {
            throw new IllegalStateException(
                format("Проблема при создании файла '%s'. Невозможно создать родительскую директорию '%s'", filePath, filePath.getParent()),
                ex
            );
        }

        try {
            Files.createFile(filePath);

        } catch (FileAlreadyExistsException ex) {

        } catch (IOException ex) {
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
