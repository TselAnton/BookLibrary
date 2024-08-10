package com.tsel.home.project.booklibrary.utils;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.lang.String.format;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.repository.FileStorageName;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Утилиты для работы с файлами хранилища
 */
public final class StorageFileUtils {

    private static final Logger log = LogManager.getLogger(StorageFileUtils.class);

    private static final Gson GSON = new Gson();

    private StorageFileUtils() {}

    /**
     * Чтение сущностей из фала-хранилища
     * @param filename Название фала-хранилища
     * @param entityClass Тип класса сущности
     * @return Список сущностей
     * @param <T> Тип сущности
     */
    public static <T> List<T> readStorageFile(String filename, Class<T> entityClass) {
        if (isStorageFileNotExist(filename)) {
            log.warn("Not found file '{}'. Can't read anything, so returning empty list", filename);
            return new ArrayList<>();
        }

        try (Stream<String> fileLines = Files.lines(Paths.get(filename), StandardCharsets.UTF_8)) {
            return fileLines
                .map(jsonEntity -> GSON.fromJson(jsonEntity, entityClass))
                .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Exception while trying to read storage file '{}' line by line", filename);
            throw new IllegalStateException(format("Проблема при чтении файла хранилища '%s'", filename), e);
        }
    }

    /**
     * Перезапись контента фала-хранилища
     * @param filename Название фала-хранилища
     * @param entities Список сущностей
     * @param <T> Тип сущности
     */
    public static <T> void overwriteStorageFile(String filename, Collection<T> entities) {
        if (isStorageFileNotExist(filename)) {
            log.info("File '{}' not exist, trying to create new one", filename);
            createStorageFile(filename);
        }

        // TODO: нужно перезаписывать файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false))) {
            Iterator<T> entitiesIterator = entities.iterator();
            while (entitiesIterator.hasNext()) {
                writer.write(GSON.toJson(entitiesIterator.next()));
                if (entitiesIterator.hasNext()) {
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            log.error("Exception while trying to write storage file '{}' line by line", filename);
            throw new IllegalStateException(format("Проблема при записи данных в файл хранилища '%s'", filename), e);
        }
    }

    /**
     * Определить тип класса
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

    private static void createStorageFile(String filename) {
        try {
            log.info("Trying to create storage file '{}'", filename);
            Files.createFile(Paths.get(filename));

        } catch (IOException e) {
            log.error("Exception while trying to create storage file '{}'", filename);
            throw new IllegalStateException(format("Проблема при создании файла хранилища '%s'", filename), e);
        }
    }

    private static boolean isStorageFileNotExist(String filename) {
        return !Files.exists(Paths.get(filename));
    }
}
