package com.tsel.home.project.booklibrary.provider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.TestEntity;
import com.tsel.home.project.booklibrary.TestFileRepository;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileRepositoryProviderTest {

    private static final String REPOSITORY_FILE = "test-repository.json";

    private static final Gson GSON = MyGson.buildGson();

    @Test
    void readStorageFile_shouldDoNothingWhenFileNotExists(@TempDir Path tempDir) {
        FileRepositoryProvider<String, TestEntity> fileRepositoryProvider = new FileRepositoryProvider<>(TestRepository.class, TestEntity.class, tempDir);

        assertFalse(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        List<TestEntity> testEntities = fileRepositoryProvider.readStorageFile();
        assertNotNull(testEntities);

        // Файл не должен пересоздаться
        assertFalse(Files.exists(tempDir.resolve(REPOSITORY_FILE)));
    }

    @Test
    void readStorageFile_shouldReadStorageFile(@TempDir Path tempDir) {
        List<TestEntity> uploadedTestEntities = List.of(
            new TestEntity("test1", 11),
            new TestEntity("test2", 22),
            new TestEntity("test3", 33)
        );

        String stringArray = GSON.toJson(uploadedTestEntities);
        writeFileContent(stringArray, tempDir.resolve(REPOSITORY_FILE));
        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        FileRepositoryProvider<String, TestEntity> fileRepositoryProvider = new FileRepositoryProvider<>(TestRepository.class, TestEntity.class, tempDir);

        List<TestEntity> testEntities = fileRepositoryProvider.readStorageFile();
        assertNotNull(testEntities);
        assertThat(testEntities)
            .hasSize(3)
            .contains(uploadedTestEntities.toArray(new TestEntity[] {}));

        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));
        assertEquals(stringArray, readFileContent(tempDir.resolve(REPOSITORY_FILE)));
    }

    @Test
    void overwriteStorageFile_shouldCreateFileAndWriteContent(@TempDir Path tempDir) {
        List<TestEntity> uploadedTestEntities = List.of(
            new TestEntity("test1", 11),
            new TestEntity("test2", 22),
            new TestEntity("test3", 33)
        );

        assertFalse(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        FileRepositoryProvider<String, TestEntity> fileRepositoryProvider = new FileRepositoryProvider<>(TestRepository.class, TestEntity.class, tempDir);
        fileRepositoryProvider.overwriteStorageFile(uploadedTestEntities);

        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));
        assertEquals(GSON.toJson(uploadedTestEntities), readFileContent(tempDir.resolve(REPOSITORY_FILE)));
    }

    @Test
    void overwriteStorageFile_shouldReplaceContentOfCreatedFile(@TempDir Path tempDir) {
        assertFalse(Files.exists(tempDir.resolve(REPOSITORY_FILE)));
        List<TestEntity> uploadedTestEntities = List.of(
            new TestEntity("test1", 11),
            new TestEntity("test2", 22),
            new TestEntity("test3", 33)
        );

        // Перезаписываем файл
        writeFileContent(GSON.toJson(uploadedTestEntities), tempDir.resolve(REPOSITORY_FILE));
        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        String fileContent = readFileContent(tempDir.resolve(REPOSITORY_FILE));
        assertNotNull(fileContent);

        // Пробуем перезаписать файл и проверяем, что там что-то другое
        List<TestEntity> newTestEntitiesList = List.of(
            new TestEntity("test4", 44),
            new TestEntity("test5", 55),
            new TestEntity("test6", 66)
        );

        FileRepositoryProvider<String, TestEntity> fileRepositoryProvider = new FileRepositoryProvider<>(TestRepository.class, TestEntity.class, tempDir);
        fileRepositoryProvider.overwriteStorageFile(newTestEntitiesList);

        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));
        assertNotNull(fileContent, readFileContent(tempDir.resolve(REPOSITORY_FILE)));
    }

    @Test
    void overwriteStorageFile_shouldCorrectMapping(@TempDir Path tempDir) {
        List<TestEntity> testEntityList = List.of(
            new TestEntity("test1", 11),
            new TestEntity("test2", 22),
            new TestEntity("test3", 33)
        );

        Map<String, TestEntity> testEntityMap = testEntityList.stream()
            .collect(Collectors.toMap(TestEntity::getId, Function.identity()));

        assertFalse(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        FileRepositoryProvider<String, TestEntity> fileRepositoryProvider = new FileRepositoryProvider<>(TestRepository.class, TestEntity.class, tempDir);
        fileRepositoryProvider.overwriteStorageFile(testEntityMap.values());

        assertTrue(Files.exists(tempDir.resolve(REPOSITORY_FILE)));

        List<TestEntity> actualTestEntitiesList = fileRepositoryProvider.readStorageFile();
        assertThat(actualTestEntitiesList).containsExactlyInAnyOrder(testEntityList.toArray(new TestEntity[]{}));
    }

    @SneakyThrows
    private static String readFileContent(Path path) {
        return Files.readString(path);
    }

    @SneakyThrows
    private static void writeFileContent(String content, Path path) {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    @FileStorageName(REPOSITORY_FILE)
    protected static class TestRepository extends TestFileRepository<TestEntity> {}
}