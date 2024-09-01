package com.tsel.home.project.booklibrary;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;

public final class TestFileUtils {

    private TestFileUtils() {}

    @SneakyThrows
    public static String readFileContent(Path path) {
        return Files.readString(path);
    }

    @SneakyThrows
    public static void writeFileContent(String content, Path path) {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
