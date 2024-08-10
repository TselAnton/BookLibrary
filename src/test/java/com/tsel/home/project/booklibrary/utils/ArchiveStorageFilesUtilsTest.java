package com.tsel.home.project.booklibrary.utils;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.tsel.home.project.booklibrary.data.BaseEntity;
import com.tsel.home.project.booklibrary.repository.FileRepository;
import com.tsel.home.project.booklibrary.repository.FileStorageName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchiveStorageFilesUtilsTest {

    private static final String UNZIP_FILE_PREFIX = "unzip-";

    private static final String TEST_FILE_1_NAME = "test1.json";
    private static final String TEST_FILE_2_NAME = "test2.json";
    private static final String TEST_FILE_3_NAME = "test3.json";
    private static final String TEST_FILE_4_NAME = "test4.json";

    @Test
    public void testArchiveStorages(@TempDir Path tempDirectory) {
        createTempFile(tempDirectory, TEST_FILE_1_NAME, "{\"test1\": \"value1\"}");
        createTempFile(tempDirectory, TEST_FILE_2_NAME, "{\"test2\": \"value2\"}");
        createTempFile(tempDirectory, TEST_FILE_3_NAME, "invalid value");

        ArchiveStorageFilesUtils.archiveStorages(tempDirectory, tempDirectory);

        File archive = getArchive(tempDirectory);
        assertNotNull("Не найден архив", archive);

        unzipArchiveFiles(tempDirectory, archive);

        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_1_NAME);
        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_2_NAME);
        assertActualAndUnzippedFilesHasEqualsContent(tempDirectory, TEST_FILE_3_NAME);
    }

    private static File getArchive(Path tempDirectory) {
        return Arrays.stream(tempDirectory.toFile().listFiles())
            .filter(File::isFile)
            .filter(file -> file.getName().startsWith("storage-archive") && file.getName().endsWith("zip"))
            .findFirst()
            .orElse(null);
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
    protected class TestRepository1 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_2_NAME)
    protected class TestRepository2 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_3_NAME)
    protected class TestRepository3 extends TestFileRepository {}

    @FileStorageName(TEST_FILE_4_NAME)
    protected class TestRepository4 extends TestFileRepository {}

    protected abstract class TestFileRepository implements FileRepository<String, BaseEntity<String>> {

        @Override
        public BaseEntity<String> getByName(String key) {
            return null;
        }

        @Override
        public List<BaseEntity<String>> getAll() {
            return List.of();
        }

        @Override
        public void save(BaseEntity<String> entity) {

        }

        @Override
        public void delete(BaseEntity<String> entity) {

        }
    }
}