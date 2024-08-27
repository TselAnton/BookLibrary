package com.tsel.home.project.booklibrary.dao.repository.utils;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

public final class ArchiveStorageFilesUtils {

    private static final Logger log = LogManager.getLogger(ArchiveStorageFilesUtils.class);

    private static final int BUFFER_SIZE = 1024;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    private ArchiveStorageFilesUtils() {}

    /**
     * Архивирование всех файлов репозиториев
     */
    public static void archiveStorages(Path fileDirectory, Path archiveDirectory) {
        log.info("Trying to archive current storage files");

        // Поиск всех репозиториев для получения названия файлов
        Reflections reflections = new Reflections("com.tsel.home.project.booklibrary");
        Set<Class<?>> repositoryClasses = reflections.get(SubTypes.of(TypesAnnotated.with(FileStorageName.class)).asClass());

        String repositoryClassesStringArray = repositoryClasses.stream().map(Class::getName).collect(Collectors.joining(", "));
        log.info("Found repository classes: [{}]", repositoryClassesStringArray);

        Path archivePath;
        try {
            // Создание архива
            archivePath = buildArchivePath(fileDirectory, archiveDirectory);
            log.info("Resolved archive path: {}", archivePath.toAbsolutePath());

        } catch (Exception e) {
            log.error("Exception while trying to create archive directory", e);
            throw new IllegalStateException("Can't create archive directory", e);
        }

        log.info("Trying to create archive with name: {}", archivePath.getFileName());
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {

            // Попытка собрать в архив все найденные файлы
            for (Class<?> repositoryClass : repositoryClasses) {
                log.info("Trying to read storage file for repository class {}", repositoryClass.getName());
                String fileStorageName = FileRepositoryUtils.resolveStorageFileName(repositoryClass);
                Path resolvedStoragePath = FileRepositoryUtils.resolveStoragePath(repositoryClass, fileDirectory);

                if (fileStorageName == null || resolvedStoragePath == null || !Files.exists(resolvedStoragePath)) {
                    log.warn("Not found storage file name for repository class {} by name '{}'. Skip it", repositoryClass.getName(), fileStorageName);
                    continue;
                }

                // Запись файла в архив
                log.info("Founded storage file '{}' for repository class {}", fileStorageName, repositoryClass.getName());
                writeFileToZipStream(resolvedStoragePath, repositoryClass, fileStorageName, zipOutputStream);
            }

        } catch (Exception e) {
            log.error("Exception while trying to archive storage files", e);
            throw new IllegalStateException("Can't archive storage files", e);
        }
    }

    private static void writeFileToZipStream(
        Path resolvedStoragePath,
        Class<?> repositoryClass,
        String fileStorageName,
        ZipOutputStream zipOutputStream
    ) throws IOException {

        try (FileInputStream fileInputStream = new FileInputStream(resolvedStoragePath.toFile())) {
            log.info("Trying to write storage file {} for repository class {} into archive", fileStorageName, repositoryClass.getName());
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

    private static Path buildArchivePath(Path directory, Path archiveDirectory) throws IOException {
        archiveDirectory = directory.resolve(archiveDirectory);
        if (!Files.exists(archiveDirectory)) {
            Files.createDirectory(archiveDirectory);
        }
        return archiveDirectory.resolve(("storage-archive-" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + ".zip"));
    }
}
