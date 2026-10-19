package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApplicationPaths {

    private ApplicationPaths() {}

    public static Path applicationJar() {
        try {
            Path path = Path.of(ApplicationPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar") ? path.toRealPath() : null;
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Не удалось определить файл приложения.", e);
        }
    }

    public static Path dataDirectory() {
        String configured = System.getProperty("booklibrary.dataDirectory");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        Path jar = applicationJar();
        if (jar != null) {
            Path location = jar.getParent().resolve(UpdateFiles.DATA_LOCATION);
            if (Files.exists(location)) {
                try {
                    return Path.of(UpdateFiles.readProperties(location).getProperty("dataDirectory")).toAbsolutePath().normalize();
                } catch (IOException | RuntimeException e) {
                    throw new IllegalStateException("Повреждён файл расположения библиотеки: " + location, e);
                }
            }
        }
        return Path.of("").toAbsolutePath().normalize();
    }
}
