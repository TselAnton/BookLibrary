package com.tsel.home.project.booklibrary.dao.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageSchemaInitializerTest {

    @TempDir
    Path directory;

    @Test
    void migratesAllEntitiesAndFillsDatesThroughSchema() throws Exception {
        writeLegacy();
        new StorageSchemaInitializer(clock(1234)).initialize(directory);
        JsonObject book = read("bookStorage.json").get(0).getAsJsonObject();
        assertEquals(1234L, book.get("createdAt").getAsLong());
        assertEquals(1234L, book.get("updatedAt").getAsLong());
        assertTrue(book.get("readUpdatedAt").isJsonNull());
        assertTrue(book.getAsJsonArray("audioBookSiteIds").isEmpty());
        assertEquals(0, book.get("publicationYear").getAsInt());
        JsonObject cycle = read("bookCycleStorage.json").get(0).getAsJsonObject();
        assertFalse(cycle.get("ended").getAsBoolean());
        assertEquals(1, cycle.get("booksInCycle").getAsInt());
        JsonObject versions = read("userSettingsStorage.json").get(0).getAsJsonObject().getAsJsonObject("appliedSchemaVersions");
        assertEquals(7, versions.size());
        for (StorageSchemas.Definition definition : StorageSchemas.definitions()) {
            assertEquals(1, versions.get(definition.schema().name()).getAsInt());
            for (com.google.gson.JsonElement entity : read(definition.fileName())) {
                assertFalse(entity.getAsJsonObject().has("obsolete"));
            }
        }
    }

    @Test
    void currentVersionsSkipNormalizationAndLowerVersionTriggersIt() throws Exception {
        writeLegacy();
        new StorageSchemaInitializer(clock(1000)).initialize(directory);
        JsonArray books = read("bookStorage.json");
        books.get(0).getAsJsonObject().addProperty("manual", true);
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        Map<String, byte[]> original = contents();
        new StorageSchemaInitializer(clock(2000)).initialize(directory);
        for (Map.Entry<String, byte[]> entry : original.entrySet()) {
            assertArrayEquals(entry.getValue(), Files.readAllBytes(directory.resolve(entry.getKey())));
        }
        JsonArray settings = read("userSettingsStorage.json");
        settings.get(0).getAsJsonObject().getAsJsonObject("appliedSchemaVersions").addProperty("book", 0);
        Files.writeString(directory.resolve("userSettingsStorage.json"), settings.toString());
        new StorageSchemaInitializer(clock(3000)).initialize(directory);
        JsonObject book = read("bookStorage.json").get(0).getAsJsonObject();
        assertFalse(book.has("manual"));
        assertEquals(1000L, book.get("createdAt").getAsLong());
    }

    @Test
    void invalidDataLeavesAllFilesUnchanged() throws Exception {
        writeLegacy();
        JsonArray books = read("bookStorage.json");
        books.get(0).getAsJsonObject().addProperty("publicationYear", "2020");
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        Map<String, byte[]> original = contents();
        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> new StorageSchemaInitializer(clock(1000)).initialize(directory));
        assertTrue(error.getMessage().contains("publicationYear"));
        for (Map.Entry<String, byte[]> entry : original.entrySet()) {
            assertArrayEquals(entry.getValue(), Files.readAllBytes(directory.resolve(entry.getKey())));
        }
    }

    @Test
    void futureVersionDoesNotModifyFiles() throws Exception {
        writeLegacy();
        Files.writeString(directory.resolve("userSettingsStorage.json"), "[{\"appliedSchemaVersions\":{\"book\":2}}]");
        Map<String, byte[]> original = contents();
        assertThrows(IllegalStateException.class, () -> new StorageSchemaInitializer().initialize(directory));
        for (Map.Entry<String, byte[]> entry : original.entrySet()) {
            assertArrayEquals(entry.getValue(), Files.readAllBytes(directory.resolve(entry.getKey())));
        }
    }

    @Test
    void missingRequiredReferenceDoesNotInventIdentifier() throws Exception {
        writeLegacy();
        JsonArray books = read("bookStorage.json");
        books.get(0).getAsJsonObject().addProperty("authorId", UUID.randomUUID().toString());
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        assertThrows(IllegalStateException.class, () -> new StorageSchemaInitializer().initialize(directory));
        assertFalse(read("userSettingsStorage.json").get(0).getAsJsonObject().has("appliedSchemaVersions"));
    }

    @Test
    void aliasesNullPoliciesAndDynamicDefaultsAreGeneric() {
        EntitySchema schema = new EntitySchema(Sample.class);
        JsonObject source = JsonParser.parseString("{\"oldName\":\"value\",\"keep\":null,\"replace\":null}").getAsJsonObject();
        JsonObject result = schema.normalize(source, clock(123));
        assertEquals("value", result.get("newName").getAsString());
        assertTrue(result.get("keep").isJsonNull());
        assertEquals(7, result.get("replace").getAsInt());
        assertEquals(123, result.get("time").getAsLong());
        assertFalse(result.has("oldName"));
        source.addProperty("newName", "conflict");
        assertThrows(IllegalArgumentException.class, () -> schema.normalize(source, clock(123)));
    }

    @Test
    void freshDirectoryInitializesSettingsOnce() throws Exception {
        new StorageSchemaInitializer().initialize(directory);
        assertEquals(7, read("userSettingsStorage.json").get(0).getAsJsonObject().getAsJsonObject("appliedSchemaVersions").size());
        assertFalse(Files.exists(directory.resolve("bookStorage.json")));
    }

    @Test
    void existingDatesArePreservedAndDuplicateIdsAreRejected() throws Exception {
        writeLegacy();
        JsonArray books = read("bookStorage.json");
        JsonObject book = books.get(0).getAsJsonObject();
        book.addProperty("createdAt", 100);
        book.addProperty("updatedAt", 200);
        book.addProperty("readUpdatedAt", 150);
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        new StorageSchemaInitializer(clock(3000)).initialize(directory);
        JsonObject migrated = read("bookStorage.json").get(0).getAsJsonObject();
        assertEquals(100, migrated.get("createdAt").getAsInt());
        assertEquals(200, migrated.get("updatedAt").getAsInt());
        assertEquals(150, migrated.get("readUpdatedAt").getAsInt());
        books.add(book.deepCopy());
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        Files.writeString(directory.resolve("userSettingsStorage.json"), "[{}]");
        assertThrows(IllegalStateException.class, () -> new StorageSchemaInitializer().initialize(directory));
    }

    @Test
    void migratesExternalFixtureCopyWhenRequested() throws Exception {
        String fixture = System.getProperty("booklibrary.schemaFixture");
        org.junit.jupiter.api.Assumptions.assumeTrue(fixture != null);
        for (StorageSchemas.Definition definition : StorageSchemas.definitions()) {
            Path source = Path.of(fixture).resolve(definition.fileName());
            if (Files.exists(source)) {
                Files.copy(source, directory.resolve(definition.fileName()));
            }
        }
        int count = read("bookStorage.json").size();
        new StorageSchemaInitializer(clock(1234)).initialize(directory);
        assertEquals(count, read("bookStorage.json").size());
        for (com.google.gson.JsonElement element : read("bookStorage.json")) {
            JsonObject book = element.getAsJsonObject();
            assertFalse(book.get("createdAt").isJsonNull());
            assertFalse(book.get("updatedAt").isJsonNull());
        }
        byte[] migrated = Files.readAllBytes(directory.resolve("bookStorage.json"));
        new StorageSchemaInitializer(clock(5678)).initialize(directory);
        assertArrayEquals(migrated, Files.readAllBytes(directory.resolve("bookStorage.json")));
    }

    @Test
    void uuidReferencesAndDuplicatesUseTheSameEqualityAsRepositories() throws Exception {
        writeLegacy();
        JsonArray authors = read("authorStorage.json");
        JsonObject author = authors.get(0).getAsJsonObject();
        author.addProperty("id", author.get("id").getAsString().toUpperCase(java.util.Locale.ROOT));
        Files.writeString(directory.resolve("authorStorage.json"), authors.toString());
        new StorageSchemaInitializer(clock(1000)).initialize(directory);

        JsonObject duplicate = author.deepCopy();
        duplicate.addProperty("id", author.get("id").getAsString().toLowerCase(java.util.Locale.ROOT));
        duplicate.addProperty("name", "Другой автор с тем же UUID");
        authors.add(duplicate);
        Files.writeString(directory.resolve("authorStorage.json"), authors.toString());
        Files.writeString(directory.resolve("userSettingsStorage.json"), "[{}]");
        byte[] original = Files.readAllBytes(directory.resolve("authorStorage.json"));
        assertThrows(IllegalStateException.class, () -> new StorageSchemaInitializer().initialize(directory));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("authorStorage.json")));
    }

    @Test
    void numericRepresentationsCannotHideDuplicateBooks() throws Exception {
        writeLegacy();
        JsonArray books = read("bookStorage.json");
        JsonObject duplicate = books.get(0).getAsJsonObject().deepCopy();
        duplicate.addProperty("id", UUID.randomUUID().toString());
        duplicate.add("publicationYear", JsonParser.parseString("0.0"));
        books.add(duplicate);
        Files.writeString(directory.resolve("bookStorage.json"), books.toString());
        byte[] original = Files.readAllBytes(directory.resolve("bookStorage.json"));
        assertThrows(IllegalStateException.class, () -> new StorageSchemaInitializer().initialize(directory));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("bookStorage.json")));
    }

    private void writeLegacy() throws Exception {
        String author = UUID.randomUUID().toString();
        String publisher = UUID.randomUUID().toString();
        for (StorageSchemas.Definition definition : StorageSchemas.definitions()) {
            JsonObject entity = new JsonObject();
            String name = definition.schema().name();
            if (!name.equals("userSettings")) {
                entity.addProperty("id", name.equals("author") ? author : name.equals("publisher") ? publisher : UUID.randomUUID().toString());
                entity.addProperty("name", name);
            }
            if (name.equals("book")) {
                entity.addProperty("authorId", author);
                entity.addProperty("publisherId", publisher);
                entity.add("createdAt", com.google.gson.JsonNull.INSTANCE);
            }
            entity.addProperty("obsolete", true);
            JsonArray array = new JsonArray();
            array.add(entity);
            Files.writeString(directory.resolve(definition.fileName()), array.toString());
        }
    }

    private JsonArray read(String name) throws Exception {
        return JsonParser.parseString(Files.readString(directory.resolve(name))).getAsJsonArray();
    }

    private Map<String, byte[]> contents() throws Exception {
        Map<String, byte[]> result = new HashMap<>();
        for (StorageSchemas.Definition definition : StorageSchemas.definitions()) {
            result.put(definition.fileName(), Files.readAllBytes(directory.resolve(definition.fileName())));
        }
        return result;
    }

    private Clock clock(long millis) {
        return Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }

    @StorageEntity(value = "sample", displayName = "Пример", version = 1)
    private static class Sample {

        @SerializedName(value = "newName", alternate = {"oldName"})
        private String name;

        @StorageField(defaultJson = "7")
        private Integer keep;

        @StorageField(defaultJson = "7", onNull = NullPolicy.USE_DEFAULT)
        private Integer replace;

        @Property(value = "time", nullable = false)
        @StorageField(provider = CurrentTimeDefault.class)
        private Long time;
    }
}
