package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class UpdateFixtures {

    public static final String PLATFORM = "windows-x86_64";

    private UpdateFixtures() {}

    public static Path jar(Path file, String version) throws IOException {
        Manifest manifest = new Manifest();
        Attributes values = manifest.getMainAttributes();
        values.putValue("Manifest-Version", "1.0");
        values.putValue("Main-Class", UpdateFiles.MAIN_CLASS);
        values.putValue("Implementation-Version", version);
        values.putValue("BookLibrary-Update-Protocol", "1");
        values.putValue("BookLibrary-Platform", PLATFORM);
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(file), manifest)) {
            output.putNextEntry(new JarEntry(UpdateFiles.MAIN_CLASS.replace('.', '/') + ".class"));
            output.write(("fixture-" + version).getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return file;
    }

    public static Path plan(Path directory, long pid, long started) throws IOException {
        Path installation = Files.createDirectories(directory.resolve("Программа с пробелом")).toRealPath();
        Path data = Files.createDirectories(directory.resolve("Каталог данных")).toRealPath();
        Files.createDirectories(data.resolve("repository/nested"));
        Files.writeString(data.resolve("repository/nested/custom.json"), "[{\"name\":\"Книга\"}]");
        Files.writeString(data.resolve(".properties"), "maxArchivedBackups=0\n");
        Path old = jar(installation.resolve("BookLibraryApp.jar"), "5.0.0");
        Path staged = Files.createDirectories(directory.resolve("Временная папка")).toRealPath();
        Path candidate = jar(staged.resolve("candidate.jar"), "6.0.0");
        UpdatePlan plan = new UpdatePlan(old, candidate, data, installation.resolve("backup/updates/test-update"), "6.0.0", PLATFORM,
            UpdateFiles.sha256(candidate), UpdateFiles.sha256(old), pid, started);
        Path file = staged.resolve("plan.properties");
        plan.write(file);
        return file;
    }
}
