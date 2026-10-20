package com.tsel.home.project.booklibrary.update.shared;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public record UpdatePlan(
    Path target, Path source, Path dataDirectory, Path backup, String version, String platform,
    String sha256, String originalSha256, long processId, long processStarted
) {

    public void write(Path file) throws IOException {
        Properties values = new Properties();
        values.setProperty("protocol", "1");
        values.setProperty("target", target.toString());
        values.setProperty("source", source.toString());
        values.setProperty("data", dataDirectory.toString());
        values.setProperty("backup", backup.toString());
        values.setProperty("version", version);
        values.setProperty("platform", platform);
        values.setProperty("sha256", sha256);
        values.setProperty("originalSha256", originalSha256);
        values.setProperty("pid", Long.toString(processId));
        values.setProperty("started", Long.toString(processStarted));
        UpdateFiles.writeProperties(file, values);
    }

    public static UpdatePlan read(Path file) throws IOException {
        try {
            Properties values = UpdateFiles.readProperties(file);
            if (!"1".equals(values.getProperty("protocol"))) {
                throw new IllegalArgumentException("Неподдерживаемый протокол");
            }
            return new UpdatePlan(
                path(values, "target"), path(values, "source"), path(values, "data"), path(values, "backup"),
                required(values, "version"), required(values, "platform"), required(values, "sha256"),
                required(values, "originalSha256"), Long.parseLong(required(values, "pid")), Long.parseLong(required(values, "started"))
            );
        } catch (IllegalArgumentException e) {
            throw new IOException("Повреждён план обновления.", e);
        }
    }

    public Path installation() {
        return target.getParent();
    }

    public void validate() throws IOException {
        if (!sha256.matches("[0-9a-f]{64}") || !originalSha256.matches("[0-9a-f]{64}")
            || Version.parse(version).suffix() != null || processId <= 0 || processStarted <= 0
            || !target.getFileName().toString().endsWith(".jar")
            || !backup.getParent().equals(installation().resolve("backup/updates"))
            || source.equals(target) || !dataDirectory.toRealPath().equals(dataDirectory)
            || !installation().toRealPath().equals(installation())) {
            throw new IOException("Недопустимые пути или параметры обновления.");
        }
        UpdateFiles.validateJar(target, null, null);
        String currentHash = UpdateFiles.sha256(target);
        if (!currentHash.equals(originalSha256) && !currentHash.equals(sha256)) {
            throw new IOException("Установленный JAR изменился. Обновление остановлено.");
        }
    }

    private static Path path(Properties values, String key) {
        Path value = Path.of(required(values, key));
        if (!value.isAbsolute() || !value.normalize().equals(value)) {
            throw new IllegalArgumentException("Ожидался абсолютный нормализованный путь");
        }
        return value;
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Отсутствует " + key);
        }
        return value;
    }
}
