package com.tsel.home.project.booklibrary.helper;

import static com.tsel.home.project.booklibrary.utils.FileUtils.isNotExists;
import static java.lang.String.format;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.utils.FileUtils;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилиты для работы с файлами хранилища
 */
//@Slf4j
public final class FileRepositoryProvider {

    private static final Gson GSON = MyGson.buildGson();

//    private final Type listType;
    private final Path fileRepositoryPath;

    public FileRepositoryProvider(Path rootPath) {
//        listType = TypeToken.getParameterized(List.class, entityClass).getType();
        fileRepositoryPath = rootPath;
    }

    /**
     * Перезапись контента фала-хранилища
     * @param entities Список сущностей
     */
    public static void overwriteStorageFile(Collection<Map<String, Object>> entities, Path fileRepositoryPath) {
        if (isNotExists(fileRepositoryPath)) {
//            log.info("File '{}' not exist, trying to create new one", fileRepositoryPath);
            FileUtils.createFile(fileRepositoryPath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileRepositoryPath.toFile(), false))) {
            writer.write(GSON.toJson(entities));

        } catch (IOException e) {
//            log.error("Exception while trying to write storage file '{}' line by line", fileRepositoryPath);
            throw new IllegalStateException(format("Проблема при записи данных в файл хранилища '%s'", fileRepositoryPath), e);
        }
    }
}
