package com.tsel.home.project.booklibrary.utils;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import com.tsel.home.project.booklibrary.controller.BookInfoViewController;
import com.tsel.home.project.booklibrary.repository.FileStorageName;
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

public final class ArchiveRepositoryStorageUtils {

    private static final Logger log = LogManager.getLogger(BookInfoViewController.class);

    private static final int BUFFER_SIZE = 1024;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    /**
     * Архивирование всех файлов репозиториев
     */
    public static void archiveStorages(Path fileDirectory, Path archiveDirectory) {
        log.info("Trying to archive current storage files");

        // Поиск всех репозиториев для получения названия файлов
        Reflections reflections = new Reflections("com.tsel.home.project.booklibrary");
        Set<Class<?>> repositoryClasses = reflections.get(SubTypes.of(TypesAnnotated.with(FileStorageName.class)).asClass());
        log.info("Found repository classes: [{}]", repositoryClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));

        // Создание архива
        Path archivePath = buildArchivePath(archiveDirectory);
        log.info("Trying to create archive with name: {}", archivePath.getFileName());
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {

            // Попытка собрать в архив все найденные файлы
            for (Class<?> repositoryClass : repositoryClasses) {
                log.info("Trying to read storage file for repository class {}", repositoryClass.getName());
                FileStorageName fileStorageNameAnnotation = repositoryClass.getAnnotation(FileStorageName.class);
                if (isFileStorageForRepositoryNotExist(fileDirectory, fileStorageNameAnnotation)) {
                    log.warn("Not found storage file name for repository class {}. Skip it", repositoryClass.getName());
                    continue;
                }

                // Запись файла в архив
                log.info("Founded storage file '{}' for repository class {}", fileStorageNameAnnotation.value(), repositoryClass.getName());
                writeFileToZipStream(fileDirectory, repositoryClass, fileStorageNameAnnotation, zipOutputStream);
            }

        } catch (Exception e) {
            log.error("Exception while trying to archive storage files", e);
            throw new IllegalStateException("Can't archive storage files", e);
        }
    }

    private static void writeFileToZipStream(
        Path fileDirectory,
        Class<?> repositoryClass,
        FileStorageName fileStorageNameAnnotation,
        ZipOutputStream zipOutputStream
    ) throws IOException {

        try (FileInputStream fileInputStream = new FileInputStream(fileDirectory.resolve(fileStorageNameAnnotation.value()).toFile())) {
            log.info("Trying to write storage file {} for repository class {} into archive",
                fileStorageNameAnnotation.value(), repositoryClass.getName()
            );
            ZipEntry zipEntry = new ZipEntry(fileStorageNameAnnotation.value());
            zipOutputStream.putNextEntry(zipEntry);

            int length;
            byte[] bytes = new byte[BUFFER_SIZE];
            while((length = fileInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }

            zipOutputStream.closeEntry();
            log.info("Storage file {} was successfully wrote to archive", fileStorageNameAnnotation.value());
        }
    }

    private static Path buildArchivePath(Path directory) {
        return directory.resolve(("storage-archive-" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + ".zip"));
    }

    private static boolean isFileStorageForRepositoryNotExist(Path fileDirectory, FileStorageName fileStorageNameAnnotation) {
        return fileStorageNameAnnotation == null
            || fileStorageNameAnnotation.value() == null
            || fileStorageNameAnnotation.value().isBlank()
            || !Files.exists(fileDirectory.resolve(fileStorageNameAnnotation.value()));

    }
}
