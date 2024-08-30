package com.tsel.home.project.booklibrary.utils;

import static com.tsel.home.project.booklibrary.utils.FileUtils.isNotExists;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.lang.String.format;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилиты для работы с файлами хранилища
 */
@Slf4j
public final class FileRepositoryUtils {

    private static final Gson GSON = MyGson.buildGson();

    private FileRepositoryUtils() {}

    /**
     * Чтение сущностей из фала-хранилища
     * @param storagePath Путь файла-хранилища
     * @param entityClass Тип класса сущности
     * @return Список сущностей
     * @param <T> Тип сущности
     */
    public static <T> List<T> readStorageFile(Path storagePath, Class<T> entityClass) {
        if (isNotExists(storagePath)) {
            log.warn("Not found file '{}'. Can't read anything, so returning empty list", storagePath);
            return new ArrayList<>();
        }

        try (Stream<String> fileLines = Files.lines(storagePath, StandardCharsets.UTF_8)) {
            return fileLines
                .map(jsonEntity -> GSON.fromJson(jsonEntity, entityClass))
                .toList();

        } catch (IOException e) {
            log.error("Exception while trying to read storage file '{}' line by line", storagePath);
            throw new IllegalStateException(format("Проблема при чтении файла хранилища '%s'", storagePath), e);
        }
    }

    /**
     * Перезапись контента фала-хранилища
     * @param storagePath Название фала-хранилища
     * @param entities Список сущностей
     * @param <T> Тип сущности
     */
    public static <T> void overwriteStorageFile(Path storagePath, Collection<T> entities) {
        if (isNotExists(storagePath)) {
            log.info("File '{}' not exist, trying to create new one", storagePath);
            FileUtils.createFile(storagePath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storagePath.toFile(), false))) {
            Iterator<T> entitiesIterator = entities.iterator();
            while (entitiesIterator.hasNext()) {
                writer.write(GSON.toJson(entitiesIterator.next()));
                if (entitiesIterator.hasNext()) {
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            log.error("Exception while trying to write storage file '{}' line by line", storagePath);
            throw new IllegalStateException(format("Проблема при записи данных в файл хранилища '%s'", storagePath), e);
        }
    }

    /**
     * Определить тип класса
     * @param repositoryClass Класс репозитория
     * @param rootPath Расположение рутовой директории
     * @return {@link Path} до файла хранилища репозитория
     */
    public static Path resolveStoragePath(Class<?> repositoryClass, @Nullable Path rootPath) {
        String repositoryFileName = resolveStorageFileName(repositoryClass);
        if (repositoryFileName == null) {
            return null;
        }
        Path storagePath = Paths.get(repositoryFileName);
        return rootPath != null
            ? Path.of(rootPath.toAbsolutePath().toString(), storagePath.toString())
            : storagePath;
    }

    /**
     * Определить название файла хранилища
     * @param repositoryClass Класс репозитория
     * @return Названия файла хранилища из {@link FileStorageName} аннотации
     */
    public static String resolveStorageFileName(Class<?> repositoryClass) {
        FileStorageName fileStorageName = repositoryClass.getAnnotation(FileStorageName.class);
        if (fileStorageName == null || isBlank(fileStorageName.value())) {
            log.error("File storage name not found for class {} or value is empty", repositoryClass);
            return null;
        }
        return fileStorageName.value();
    }
}
