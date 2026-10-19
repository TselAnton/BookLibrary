package com.tsel.home.project.booklibrary.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.Version;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.DoubleConsumer;

public final class GitHubReleaseClient {

    public static final String REPOSITORY_URL = "https://github.com/TselAnton/BookLibrary";
    private static final URI LATEST = URI.create("https://api.github.com/repos/TselAnton/BookLibrary/releases/latest");
    private static final long MAX_JAR_SIZE = 512L * 1024 * 1024;

    private final ReleaseTransport transport;

    public GitHubReleaseClient() {
        this(new GitHubTransport());
    }

    GitHubReleaseClient(ReleaseTransport transport) {
        this.transport = transport;
    }

    public Release check(Version current, String platform) throws IOException, InterruptedException {
        try {
            JsonObject release = JsonParser.parseString(new String(transport.read(LATEST, 1024 * 1024), StandardCharsets.UTF_8)).getAsJsonObject();
            if (release.get("draft").getAsBoolean() || release.get("prerelease").getAsBoolean()) {
                throw new IOException("GitHub не вернул стабильный опубликованный релиз.");
            }
            Version version = Version.parse(release.get("tag_name").getAsString());
            if (version.suffix() != null) {
                throw new IOException("У стабильного релиза некорректный тег.");
            }
            if (version.compareTo(current) <= 0) {
                return null;
            }
            JsonArray assets = release.getAsJsonArray("assets");
            JsonObject metadata = findAsset(assets, "update-" + platform + ".properties");
            Properties values = new Properties();
            values.load(new ByteArrayInputStream(transport.read(assetUri(metadata), 65536)));
            if (!"1".equals(values.getProperty("protocol")) || !version.toString().equals(values.getProperty("version"))
                || !platform.equals(values.getProperty("platform"))) {
                throw new IOException("Обновление не поддерживает эту платформу или протокол. Установите релиз вручную.");
            }
            int minimumJava = Integer.parseInt(values.getProperty("java.minimum"));
            if (minimumJava < 17 || Runtime.version().feature() < minimumJava) {
                throw new IOException("Для новой версии требуется Java " + minimumJava + " или новее.");
            }
            String name = "BookLibraryApp-" + platform + ".jar";
            if (!name.equals(values.getProperty("asset")) || !values.getProperty("sha256", "").matches("[0-9a-f]{64}")) {
                throw new IOException("Повреждены метаданные релизного JAR.");
            }
            JsonObject jar = findAsset(assets, name);
            long size = jar.get("size").getAsLong();
            if (size <= 0 || size > MAX_JAR_SIZE) {
                throw new IOException("Недопустимый размер релизного JAR.");
            }
            return new Release(version, platform, assetUri(jar), size, values.getProperty("sha256"),
                release.has("body") && !release.get("body").isJsonNull() ? release.get("body").getAsString() : "");
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException | JsonParseException e) {
            throw new IOException("Некорректный ответ GitHub или метаданные обновления.", e);
        }
    }

    public Path download(Release release, DoubleConsumer progress) throws IOException, InterruptedException {
        Path directory = Files.createTempDirectory("booklibrary-update-");
        Path file = directory.resolve("download.jar");
        try {
            transport.download(release.url(), file, release.size(), progress);
            if (Files.size(file) != release.size() || !UpdateFiles.sha256(file).equals(release.sha256())) {
                throw new IOException("Скачанный JAR не прошёл проверку контрольной суммы.");
            }
            UpdateFiles.validateJar(file, release.version().toString(), release.platform());
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Скачивание отменено");
            }
            return file;
        } catch (IOException | InterruptedException | RuntimeException e) {
            Files.deleteIfExists(file);
            throw e;
        }
    }

    private JsonObject findAsset(JsonArray assets, String name) throws IOException {
        JsonObject found = null;
        for (JsonElement value : assets) {
            JsonObject asset = value.getAsJsonObject();
            if (name.equals(asset.get("name").getAsString())) {
                if (found != null || !"uploaded".equals(asset.get("state").getAsString())) {
                    throw new IOException("Релиз содержит неоднозначный или незавершённый файл: " + name);
                }
                found = asset;
            }
        }
        if (found == null) {
            throw new IOException("В релизе отсутствует файл " + name + ". Установите релиз вручную.");
        }
        return found;
    }

    private URI assetUri(JsonObject asset) throws IOException {
        URI uri = URI.create(asset.get("browser_download_url").getAsString());
        if (!"https".equals(uri.getScheme()) || !"github.com".equals(uri.getHost()) || uri.getPort() != -1
            || uri.getUserInfo() != null || !uri.getPath().startsWith("/TselAnton/BookLibrary/releases/download/")) {
            throw new IOException("Недопустимый адрес файла обновления.");
        }
        return uri;
    }

    public record Release(Version version, String platform, URI url, long size, String sha256, String notes) {}
}
