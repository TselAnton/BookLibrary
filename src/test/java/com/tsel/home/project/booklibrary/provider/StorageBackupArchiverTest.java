package com.tsel.home.project.booklibrary.provider;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import com.tsel.home.project.booklibrary.TestDateProvider;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageBackupArchiverTest {

    private static final String UNZIP_FILE_PREFIX = "unzip-";

    private static final String TEST_FILE_1_NAME = "test1.json";
    private static final String TEST_FILE_2_NAME = "test2.json";
    private static final String TEST_FILE_3_NAME = "test3.json";
    private static final String TEST_FILE_4_NAME = "test4.json";

    private static final TestDateProvider TEST_DATE_PROVIDER = new TestDateProvider();
    private static final StorageBackupArchiver STORAGE_BACKUP_ARCHIVER = new StorageBackupArchiver();

    @BeforeEach
    public void init() {
        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.now());
    }

    @Test
    void testArchiveStorages(@TempDir Path tempDirectory) {
        SimpleApplicationContextProvider.initBean(DateProvider.class, () -> TEST_DATE_PROVIDER);

        createTempFile(tempDirectory, TEST_FILE_1_NAME, "{\"test1\": \"value1\"}");
        createTempFile(tempDirectory, TEST_FILE_2_NAME, "{\"test2\": \"value2\"}");
        createTempFile(tempDirectory, TEST_FILE_3_NAME, "invalid value");

        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        File archive = getArchive(tempDirectory);
        assertNotNull("Не найден архив", archive);

        unzipArchiveFiles(tempDirectory, archive);

        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_1_NAME);
        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_2_NAME);
        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_3_NAME);
    }

    @Test
    @SneakyThrows
    void testRemoveExpiredArchiveStorages(@TempDir Path tempDirectory) {
        SimpleApplicationContextProvider.initBean(PropertyProvider.class, () -> new PropertyProvider(tempDirectory));
        SimpleApplicationContextProvider.initBean(DateProvider.class, () -> TEST_DATE_PROVIDER);

        createTempFile(tempDirectory, TEST_FILE_1_NAME, "{\"test1\": \"value1\"}");
        createTempFile(tempDirectory, TEST_FILE_2_NAME, "{\"test2\": \"value2\"}");
        createTempFile(tempDirectory, TEST_FILE_3_NAME, "{\"test3\": \"value3\"}");
        createTempFile(tempDirectory, TEST_FILE_4_NAME, "{\"test4\": \"value4\"}");

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 8, 30, 1, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 8, 30, 1, 5, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 8, 30, 1, 5, 1));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 8, 30, 3, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 8, 31, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 9, 1, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 9, 2, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 10, 2, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 10, 5, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        TEST_DATE_PROVIDER.setFakeNow(LocalDateTime.of(2024, 10, 10, 0, 0, 0));
        STORAGE_BACKUP_ARCHIVER.archiveStorages(tempDirectory, tempDirectory);

        // Получаем все файлы с архивами и сортируем по дате создания DESC
        List<File> archiveFiles = getArchives(tempDirectory)
            .stream()
            .sorted(Comparator.comparing(this::getCreateDate).reversed())
            .toList();

        assertEquals(10, archiveFiles.size());

        // Удаляем устаревшие архивы
        STORAGE_BACKUP_ARCHIVER.removeExpiredArchives(tempDirectory);

        // Берём первые 5 файлов и сверяем, что это те же файлы, что остались после отчистки
        List<File> expectedArchives = archiveFiles.stream().limit(5).toList();
        List<File> actualArchives = getArchives(tempDirectory);

        assertThat(actualArchives, hasSize(5));
        assertThat(actualArchives, containsInAnyOrder(expectedArchives.toArray()));
    }

    private static File getArchive(Path tempDirectory) {
        return getArchives(tempDirectory)
            .stream()
            .findFirst()
            .orElse(null);
    }

    private static List<File> getArchives(Path tempDirectory) {
        return Arrays.stream(tempDirectory.toFile().listFiles())
            .filter(File::isFile)
            .filter(file -> file.getName().startsWith("storage-archive") && file.getName().endsWith("zip"))
            .toList();
    }

    private long getCreateDate(File file) {
        try {
            FileTime fileTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
            return fileTime.toMillis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTempFile(Path tempDirectory, String fileName, String content) {
        Path filePath = tempDirectory.resolve(fileName);
        try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
            fileWriter.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unzipArchiveFiles(Path tempDirectory, File archive) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(archive))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(new File(tempDirectory.toFile(), UNZIP_FILE_PREFIX + zipEntry.getName()))) {
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertActualAndUnzippedFilesHasEqualsContent(Path tempDirectory, String fileName) {
        String actualContent;
        try (FileInputStream fileInputStream = new FileInputStream(tempDirectory.resolve(fileName).toFile())) {
            actualContent = new String(fileInputStream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String unzippedContent;
        try (FileInputStream fileInputStream = new FileInputStream(tempDirectory.resolve(UNZIP_FILE_PREFIX + fileName).toFile())) {
            unzippedContent = new String(fileInputStream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(
            format("Контент файла '%s' и контент из аналогичного распакованного файла '%s' не совпадают", actualContent, unzippedContent),
            actualContent, unzippedContent
        );
    }

    @FileStorageName(TEST_FILE_1_NAME)
    protected static class TestRepository1 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_2_NAME)
    protected static class TestRepository2 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_3_NAME)
    protected static class TestRepository3 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_4_NAME)
    protected static class TestRepository4 extends TestFileRepository {}

    protected abstract static class TestFileRepository implements FileRepository<String, BaseEntity<String>> {

        @Override
        public BaseEntity<String> getById(String id) {
            return null;
        }

        @Override
        public List<BaseEntity<String>> getAll() {
            return List.of();
        }

        @Override
        public String save(BaseEntity<String> entity) {
            return null;
        }

        @Override
        public void delete(BaseEntity<String> entity) {
            // ONLY FOR TESTS
        }

        @Override
        public void deleteById(String id) {
            // ONLY FOR TESTS
        }

        @Override
        public boolean existById(String id) {
            return false;
        }

        @Override
        public void beginTransaction() {
            // ONLY FOR TESTS
        }

        @Override
        public void commitTransaction() {
            // ONLY FOR TESTS
        }

        @Override
        public void abortTransaction() {
            // ONLY FOR TESTS
        }
    }
}