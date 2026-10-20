package com.tsel.home.project.booklibrary.helper;

import static org.reflections.scanners.Scanners.TypesAnnotated;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.schema.StorageSchemas;
import com.tsel.home.project.booklibrary.utils.FileUtils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
public final class StorageBackupArchiver {

    private static final String PREFIX = "storage-archive-";
    private static final String POSTFIX = ".zip";

    private static final int DEFAULT_EXPIRED_LIMIT = 5;

    private static final int BUFFER_SIZE = 1024;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    /**
     * Архивирование всех файлов репозиториев
     */
    public void archiveStorages(Path repositoryPath, Path archivePath) {
        log.info("Trying to archive current storage files");

        // Реестр гарантирует включение файлов миграции даже при ограниченном сканировании модулей.
        Set<String> storageNames = new LinkedHashSet<>();
        StorageSchemas.definitions().forEach(definition -> storageNames.add(definition.fileName()));
        Reflections reflections = new Reflections("com.tsel.home.project.booklibrary");
        reflections.get(TypesAnnotated.with(FileStorageName.class).asClass()).stream()
            .map(FileRepositoryProvider::resolveStorageFileName)
            .filter(java.util.Objects::nonNull)
            .forEach(storageNames::add);

        try {
            // Создание директории под бекапы
            if (FileUtils.isNotExists(archivePath)) {
                Files.createDirectory(archivePath);
                log.info("Created backup directory '{}'", archivePath);
            }

        } catch (Exception e) {
            log.error("Exception while trying to create archive directory", e);
            throw new IllegalStateException("Can't create archive directory", e);
        }

        archivePath = resolveFullArchivePath(archivePath);
        log.info("Resolved archive path: {}", archivePath.toAbsolutePath());

        log.info("Trying to create archive with name: {}", archivePath.getFileName());
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {

            for (String fileStorageName : storageNames) {
                Path resolvedStoragePath = repositoryPath.resolve(fileStorageName);
                if (Files.exists(resolvedStoragePath)) {
                    writeFileToZipStream(resolvedStoragePath, fileStorageName, zipOutputStream);
                }
            }

        } catch (Exception e) {
            log.error("Exception while trying to archive storage files", e);
            throw new IllegalStateException("Can't archive storage files", e);
        }
    }

    public void removeExpiredArchives(Path archivePath) {
        try (Stream<Path> filePaths = Files.walk(archivePath, 1)) {
            filePaths.map(path -> new SortingArchivePath(parseArchiveFileName(path), path))
                .filter(sortingArchivePath -> sortingArchivePath.localDateTime() != null)
                .sorted(Comparator.comparing(SortingArchivePath::localDateTime, LocalDateTime::compareTo).reversed())
                .skip(resolveMaxArchivedBackups())
                .map(SortingArchivePath::archivePath)
                .forEach(StorageBackupArchiver::removeExpiredArchiveFile);

        } catch (IOException e) {
            log.error("Exception while remove expired backup archives", e);
        }
    }

    private void writeFileToZipStream(
        Path resolvedStoragePath,
        String fileStorageName,
        ZipOutputStream zipOutputStream
    ) throws IOException {

        try (FileInputStream fileInputStream = new FileInputStream(resolvedStoragePath.toFile())) {
            log.info("Trying to write storage file {} into archive", fileStorageName);
            ZipEntry zipEntry = new ZipEntry(fileStorageName);
            zipOutputStream.putNextEntry(zipEntry);

            int length;
            byte[] bytes = new byte[BUFFER_SIZE];
            while((length = fileInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }

            zipOutputStream.closeEntry();
            log.info("Storage file {} was successfully wrote to archive", fileStorageName);
        }
    }

    private Path resolveFullArchivePath(Path archivePath) {
        return archivePath.resolve((PREFIX + DATE_TIME_FORMATTER.format(SimpleApplicationContext.getBean(DateProvider.class).getNow()) + POSTFIX));
    }

    private Integer resolveMaxArchivedBackups() {
        try {
            return Integer.parseInt(SimpleApplicationContext.getBean(PropertyProvider.class).getProperty(PropertyProvider.MAX_ARCHIVED_BACKUPS));
        } catch (NumberFormatException e) {
            log.warn("Cant read property '{}'. Set it default as {}", PropertyProvider.MAX_ARCHIVED_BACKUPS, DEFAULT_EXPIRED_LIMIT);
            return DEFAULT_EXPIRED_LIMIT;
        }
    }

    private LocalDateTime parseArchiveFileName(Path archiveFileName) {
        String archiveName = archiveFileName.getFileName().toString();

        try {
            if (!archiveName.contains(PREFIX) || !archiveName.contains(POSTFIX)) {
                return null;
            }
            return LocalDateTime.parse(archiveName.replace(PREFIX, "").replace(POSTFIX, ""), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Can't parse archive name '{}'", archiveFileName);
            return null;
        }
    }

    private static void removeExpiredArchiveFile(Path expiredArchivePath) {
        try {
            Files.deleteIfExists(expiredArchivePath);
        } catch (Exception e) {
            log.error("Exception while trying to delete expired archive path '{}'", expiredArchivePath);
        }
    }

    private record SortingArchivePath(LocalDateTime localDateTime, Path archivePath) {}
}
