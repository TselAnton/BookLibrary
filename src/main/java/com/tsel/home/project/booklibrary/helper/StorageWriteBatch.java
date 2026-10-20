package com.tsel.home.project.booklibrary.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Запись подготовленных документов с восстановлением исходных байтов при ошибке.
 * Не защищает от завершения процесса или отключения питания во время записи.
 */
public class StorageWriteBatch {

    private final Map<Path, byte[]> documents = new LinkedHashMap<>();

    public void add(Path path, byte[] content) {
        Path target = path.toAbsolutePath().normalize();
        if (documents.putIfAbsent(target, content.clone()) != null) {
            throw new IllegalArgumentException("Повторный файл в операции: " + target);
        }
    }

    public void commit() {
        Map<Path, byte[]> originals = new LinkedHashMap<>();
        List<Path> attempted = new ArrayList<>();
        try {
            for (Path path : documents.keySet()) {
                originals.put(path, Files.exists(path) ? Files.readAllBytes(path) : null);
            }
            for (Map.Entry<Path, byte[]> entry : documents.entrySet()) {
                attempted.add(entry.getKey());
                write(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            IllegalStateException failure = new IllegalStateException("Не удалось сохранить данные", e);
            for (int i = attempted.size() - 1; i >= 0; i--) {
                Path path = attempted.get(i);
                try {
                    byte[] original = originals.get(path);
                    if (original == null) {
                        Files.deleteIfExists(path);
                    } else {
                        write(path, original);
                    }
                } catch (Exception restoreError) {
                    failure.addSuppressed(new IllegalStateException("Не удалось восстановить " + path, restoreError));
                }
            }
            if (failure.getSuppressed().length > 0) {
                throw new IllegalStateException("Не удалось восстановить файлы после ошибки записи. Требуется восстановление из резервной копии.", failure);
            }
            throw failure;
        }
    }

    protected void write(Path path, byte[] content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, content);
    }
}
