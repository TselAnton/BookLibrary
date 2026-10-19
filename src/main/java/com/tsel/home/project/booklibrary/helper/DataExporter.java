package com.tsel.home.project.booklibrary.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DataExporter {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public Path export(Path repository, Path destination) throws IOException {
        List<Path> sources;
        try (Stream<Path> paths = Files.walk(repository)) {
            sources = paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                .sorted()
                .toList();
        }
        if (sources.isEmpty()) {
            throw new IOException("В директории repository нет JSON-файлов для экспорта.");
        }

        Path archive = Files.createTempFile(destination, "booklibrary-export-" + TIMESTAMP.format(LocalDateTime.now()) + "-", ".zip");
        try (OutputStream output = Files.newOutputStream(archive);
            ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Path source : sources) {
                String name = repository.relativize(source).toString().replace('\\', '/');
                ZipEntry entry = new ZipEntry(name);
                entry.setTime(Files.getLastModifiedTime(source).toMillis());
                zip.putNextEntry(entry);
                Files.copy(source, zip);
                zip.closeEntry();
            }
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(archive);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
        return archive;
    }
}
