package com.tsel.home.project.booklibrary.update.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class UpdateFiles {

    public static final String MAIN_CLASS = "com.tsel.home.project.booklibrary.App";
    public static final String PENDING = ".booklibrary-update.pending";
    public static final String DATA_LOCATION = ".booklibrary-data.properties";

    private UpdateFiles() {}

    public static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, count);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Properties readProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

    public static void writeProperties(Path target, Properties properties) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), ".update-state-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "BookLibrary update protocol 1");
            }
            force(temporary);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static void force(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    public static void regularFile(Path file) throws IOException {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || !file.toRealPath().equals(file.toAbsolutePath().normalize())) {
            throw new IOException("Ожидался обычный файл без символических ссылок: " + file);
        }
    }

    public static void validateJar(Path file, String version, String platform) throws IOException {
        regularFile(file);
        try (JarFile jar = new JarFile(file.toFile(), true)) {
            if (jar.getManifest() == null) {
                throw new IOException("У JAR отсутствует manifest.");
            }
            java.util.jar.Attributes attributes = jar.getManifest().getMainAttributes();
            if (!MAIN_CLASS.equals(attributes.getValue("Main-Class"))
                || jar.getEntry(MAIN_CLASS.replace('.', '/') + ".class") == null) {
                throw new IOException("Файл не является приложением BookLibrary.");
            }
            if (version != null && (!version.equals(attributes.getValue("Implementation-Version"))
                || !platform.equals(attributes.getValue("BookLibrary-Platform"))
                || !"1".equals(attributes.getValue("BookLibrary-Update-Protocol")))) {
                throw new IOException("Версия, платформа или протокол JAR не соответствуют релизу.");
            }
            long total = 0;
            byte[] buffer = new byte[65536];
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                try (InputStream input = jar.getInputStream(entry)) {
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        if (total > 1024L * 1024 * 1024) {
                            throw new IOException("Слишком большой распакованный JAR.");
                        }
                    }
                }
            }
        }
    }
}
