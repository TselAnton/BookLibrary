package com.tsel.home.project.booklibrary.helper;

import static com.tsel.home.project.booklibrary.utils.FileUtils.isNotExists;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.utils.FileUtils;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилиты для работы с файлами хранилища
 */
@Slf4j
public final class FileRepositoryProvider<K extends Serializable, E extends BaseEntity<K>> {

    private static final Gson GSON = MyGson.buildGson();

    private final Type listType;
    private final Path fileRepositoryPath;

    public FileRepositoryProvider(Class<?> repositoryClass, Class<E> entityClass, @Nullable Path rootPath) {
        listType = TypeToken.getParameterized(List.class, entityClass).getType();
        fileRepositoryPath = resolveStoragePath(repositoryClass, rootPath);
    }

    /**
     * Чтение сущностей из фала-хранилища
     * @return Список сущностей
     */
    public List<E> readStorageFile() {
        if (isNotExists(fileRepositoryPath)) {
            log.warn("Not found file '{}'. Can't read anything, so returning empty list", fileRepositoryPath);
            return new ArrayList<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(fileRepositoryPath, UTF_8)) {
            List<E> entities = GSON.fromJson(reader, listType);
            if (entities == null) {
                throw new IllegalStateException("Ожидается JSON-массив: " + fileRepositoryPath);
            }
            return entities;

        } catch (IOException e) {
            log.error("Exception while trying to read storage file '{}' by bytes", fileRepositoryPath, e);
            throw new IllegalStateException(format("Проблема при чтении файла хранилища '%s'", fileRepositoryPath), e);
        }
    }

    /**
     * Перезапись контента фала-хранилища
     * @param entities Список сущностей
     */
    public void overwriteStorageFile(Collection<E> entities) {
        StorageWriteBatch batch = new StorageWriteBatch();
        prepareWrite(batch, entities);
        batch.commit();
    }

    public void prepareWrite(StorageWriteBatch batch, Collection<E> entities) {
        batch.add(fileRepositoryPath, GSON.toJson(entities).getBytes(UTF_8));
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
        return FileUtils.resolvePath(rootPath, storagePath);
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
