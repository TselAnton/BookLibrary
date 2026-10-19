package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.Version;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitHubReleaseClientTest {

    @Test
    void checksStableMetadataAndDownloadedJar(@TempDir Path directory) throws Exception {
        FakeTransport transport = new FakeTransport(directory);
        GitHubReleaseClient client = new GitHubReleaseClient(transport);
        GitHubReleaseClient.Release release = client.check(Version.parse("5.0.0-SNAPSHOT"), UpdateFixtures.PLATFORM);
        assertEquals(Version.parse("6.0.0"), release.version());
        Path downloaded = client.download(release, value -> {});
        try {
            assertEquals(UpdateFiles.sha256(transport.jar), UpdateFiles.sha256(downloaded));
        } finally {
            Files.deleteIfExists(downloaded);
        }
        transport.release.addProperty("tag_name", "v4.1.0");
        assertNull(client.check(Version.parse("5.0.0-SNAPSHOT"), UpdateFixtures.PLATFORM));
    }

    @Test
    void rejectsPrereleasesUnsupportedPlatformsAndForeignUrls(@TempDir Path directory) throws Exception {
        FakeTransport transport = new FakeTransport(directory);
        GitHubReleaseClient client = new GitHubReleaseClient(transport);
        transport.release.addProperty("prerelease", true);
        assertThrows(IOException.class, () -> client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM));
        transport.release.addProperty("prerelease", false);
        transport.metadata = transport.metadata.replace("protocol=1", "protocol=99");
        assertThrows(IOException.class, () -> client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM));
        transport.metadata = transport.metadata.replace("protocol=99", "protocol=1");
        JsonObject metadataAsset = transport.release.getAsJsonArray("assets").get(0).getAsJsonObject();
        metadataAsset.addProperty("browser_download_url", "https://example.com/update.properties");
        assertThrows(IOException.class, () -> client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM));
        assertFalse(transport.foreignRead, "Foreign URLs must be rejected before requesting them");
    }

    @Test
    void rejectsCorruptionAndRemovesPartialDownload(@TempDir Path directory) throws Exception {
        FakeTransport transport = new FakeTransport(directory);
        GitHubReleaseClient client = new GitHubReleaseClient(transport);
        GitHubReleaseClient.Release release = client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM);
        transport.corrupt = true;
        assertThrows(IOException.class, () -> client.download(release, value -> {}));
        assertFalse(Files.exists(transport.lastTarget));
        transport.corrupt = false;
        transport.metadata = transport.metadata.replace("version=6.0.0", "version=7.0.0");
        assertThrows(IOException.class, () -> client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM));
    }

    @Test
    void rejectsInvalidJsonAndWrongVersionInsideJar(@TempDir Path directory) throws Exception {
        FakeTransport transport = new FakeTransport(directory);
        GitHubReleaseClient client = new GitHubReleaseClient(transport);
        GitHubReleaseClient.Release release = client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM);
        UpdateFixtures.jar(transport.jar, "7.0.0");
        GitHubReleaseClient.Release wrongVersion = new GitHubReleaseClient.Release(release.version(), release.platform(), release.url(),
            Files.size(transport.jar), UpdateFiles.sha256(transport.jar), "");
        assertThrows(IOException.class, () -> client.download(wrongVersion, value -> {}));
        assertFalse(Files.exists(transport.lastTarget));
        transport.invalidJson = true;
        assertThrows(IOException.class, () -> client.check(Version.parse("5.0.0"), UpdateFixtures.PLATFORM));
    }

    private static class FakeTransport implements ReleaseTransport {

        private final Path jar;
        private final JsonObject release = new JsonObject();
        private String metadata;
        private boolean corrupt;
        private boolean invalidJson;
        private boolean foreignRead;
        private Path lastTarget;

        private FakeTransport(Path directory) throws Exception {
            jar = UpdateFixtures.jar(directory.resolve("release.jar"), "6.0.0");
            release.addProperty("tag_name", "v6.0.0");
            release.addProperty("draft", false);
            release.addProperty("prerelease", false);
            release.addProperty("body", "Новая версия");
            JsonArray assets = new JsonArray();
            assets.add(asset("update-" + UpdateFixtures.PLATFORM + ".properties", 200));
            assets.add(asset("BookLibraryApp-" + UpdateFixtures.PLATFORM + ".jar", Files.size(jar)));
            release.add("assets", assets);
            metadata = "protocol=1\nversion=6.0.0\nplatform=" + UpdateFixtures.PLATFORM + "\njava.minimum=17\nasset=BookLibraryApp-"
                + UpdateFixtures.PLATFORM + ".jar\nsha256=" + UpdateFiles.sha256(jar) + "\n";
        }

        @Override
        public byte[] read(URI uri, long limit) {
            if (!uri.getHost().equals("github.com") && !uri.getHost().equals("api.github.com")) {
                foreignRead = true;
            }
            String response = uri.getHost().equals("api.github.com") ? (invalidJson ? "not json" : release.toString()) : metadata;
            return response.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void download(URI uri, Path target, long expectedSize, DoubleConsumer progress) throws IOException {
            lastTarget = target;
            Files.copy(jar, target);
            if (corrupt) {
                byte[] content = Files.readAllBytes(target);
                content[content.length - 1] ^= 1;
                Files.write(target, content);
            }
            progress.accept(1);
        }

        private JsonObject asset(String name, long size) {
            JsonObject asset = new JsonObject();
            asset.addProperty("name", name);
            asset.addProperty("state", "uploaded");
            asset.addProperty("size", size);
            asset.addProperty("browser_download_url", GitHubReleaseClient.REPOSITORY_URL + "/releases/download/v6.0.0/" + name);
            return asset;
        }
    }
}
