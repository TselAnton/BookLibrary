package com.tsel.home.project.booklibrary.dao.schema;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Нормализация только тех документов, версия которых меньше текущей. */
public final class StorageSchemaInitializer {

    private static final Gson GSON = MyGson.buildGson();

    private final Clock clock;
    private final List<StorageSchemas.Definition> definitions;

    public StorageSchemaInitializer() {
        this(Clock.systemUTC());
    }

    public StorageSchemaInitializer(Clock clock) {
        this.clock = clock;
        this.definitions = StorageSchemas.definitions();
    }

    public void initialize(Path directory) {
        StorageSchemas.Definition settingsDefinition = definitions.stream()
            .filter(definition -> definition.schema().name().equals("userSettings"))
            .findFirst().orElseThrow();
        JsonArray settings = read(directory.resolve(settingsDefinition.fileName()));
        if (settings.size() > 1) {
            throw new IllegalStateException("В настройках пользователя должно быть не более одной записи");
        }
        JsonObject settingsObject = settings.isEmpty() ? new JsonObject() : settings.get(0).getAsJsonObject();
        JsonObject versions = settingsObject.has("appliedSchemaVersions") && !settingsObject.get("appliedSchemaVersions").isJsonNull()
            ? settingsObject.getAsJsonObject("appliedSchemaVersions").deepCopy() : new JsonObject();
        boolean pending = false;
        for (StorageSchemas.Definition definition : definitions) {
            int applied = appliedVersion(versions, definition.schema().name());
            if (applied > definition.schema().version()) {
                throw new IllegalStateException("Схема " + definition.schema().name() + " новее поддерживаемой приложением");
            }
            pending |= applied < definition.schema().version();
        }
        if (!pending) {
            return;
        }

        Clock migrationClock = Clock.fixed(clock.instant(), ZoneOffset.UTC);
        Map<String, JsonArray> documents = new LinkedHashMap<>();
        StorageWriteBatch batch = new StorageWriteBatch();
        for (StorageSchemas.Definition definition : definitions) {
            EntitySchema schema = definition.schema();
            boolean userSettings = schema.name().equals("userSettings");
            JsonArray original = userSettings ? settings : read(directory.resolve(definition.fileName()));
            JsonArray normalized = original.deepCopy();
            if (userSettings && normalized.isEmpty()) {
                normalized.add(new JsonObject());
            }
            if (appliedVersion(versions, schema.name()) < schema.version()) {
                JsonArray result = new JsonArray();
                for (int i = 0; i < normalized.size(); i++) {
                    JsonElement record = normalized.get(i);
                    try {
                        result.add(schema.normalize(record.getAsJsonObject(), migrationClock));
                    } catch (RuntimeException e) {
                        throw new IllegalStateException("Файл " + definition.fileName() + ", запись " + i
                            + ", id=" + (record.isJsonObject() ? record.getAsJsonObject().get("id") : "?")
                            + ": " + e.getMessage(), e);
                    }
                }
                normalized = result;
            }
            documents.put(schema.name(), normalized);
            versions.addProperty(schema.name(), schema.version());
            if (!userSettings && !original.equals(normalized)) {
                batch.add(directory.resolve(definition.fileName()), GSON.toJson(normalized).getBytes(UTF_8));
            }
        }
        validateRelations(documents);
        JsonArray updatedSettings = documents.get("userSettings");
        updatedSettings.get(0).getAsJsonObject().add("appliedSchemaVersions", versions);
        batch.add(directory.resolve(settingsDefinition.fileName()), GSON.toJson(updatedSettings).getBytes(UTF_8));
        batch.commit();
    }

    private int appliedVersion(JsonObject versions, String name) {
        if (!versions.has(name)) {
            return 0;
        }
        try {
            JsonElement value = versions.get(name);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException();
            }
            int version = value.getAsBigDecimal().intValueExact();
            if (version < 0) {
                throw new IllegalArgumentException();
            }
            return version;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Неверная версия схемы " + name, e);
        }
    }

    private JsonArray read(Path file) {
        if (!Files.exists(file)) {
            return new JsonArray();
        }
        try {
            return JsonParser.parseString(Files.readString(file, UTF_8)).getAsJsonArray();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Не удалось прочитать JSON-массив " + file, e);
        }
    }

    private void validateRelations(Map<String, JsonArray> documents) {
        Map<String, Set<String>> ids = new HashMap<>();
        for (Map.Entry<String, JsonArray> entry : documents.entrySet()) {
            if (entry.getKey().equals("userSettings")) {
                continue;
            }
            Set<String> entityIds = new HashSet<>();
            Set<String> uniqueKeys = new HashSet<>();
            for (JsonElement element : entry.getValue()) {
                JsonObject entity = element.getAsJsonObject();
                JsonElement id = entity.get("id");
                if (id == null || id.isJsonNull() || !entityIds.add(UUID.fromString(id.getAsString()).toString())) {
                    throw new IllegalStateException("Пустой или повторный id в схеме " + entry.getKey());
                }
                JsonArray key = new JsonArray();
                List<String> fields = entry.getKey().equals("book")
                    ? List.of("name", "authorId", "publisherId", "cycleId", "numberInSeries", "publicationYear")
                    : List.of("name");
                for (String field : fields) {
                    JsonElement value = entity.get(field);
                    if (field.endsWith("Id") && value != null && !value.isJsonNull()) {
                        key.add(UUID.fromString(value.getAsString()).toString());
                    } else if ((field.equals("publicationYear") || field.equals("numberInSeries"))
                        && value != null && !value.isJsonNull()) {
                        key.add(value.getAsBigDecimal().intValueExact());
                    } else {
                        key.add(value);
                    }
                }
                if (!uniqueKeys.add(key.toString())) {
                    throw new IllegalStateException("Повторная запись в схеме " + entry.getKey() + ", id=" + id);
                }
            }
            ids.put(entry.getKey(), entityIds);
        }
        for (JsonElement element : documents.get("book")) {
            JsonObject book = element.getAsJsonObject();
            checkReference(book, "authorId", "author", ids);
            checkReference(book, "publisherId", "publisher", ids);
            checkReference(book, "genreId", "genre", ids);
            checkReference(book, "cycleId", "cycle", ids);
            JsonElement sites = book.get("audioBookSiteIds");
            if (sites != null && !sites.isJsonNull()) {
                for (JsonElement site : sites.getAsJsonArray()) {
                    if (site.isJsonNull() || !ids.get("audioBookSite").contains(UUID.fromString(site.getAsString()).toString())) {
                        throw new IllegalStateException("Неизвестный аудиосайт у книги " + book.get("id"));
                    }
                }
            }
        }
    }

    private void checkReference(JsonObject book, String field, String target, Map<String, Set<String>> ids) {
        JsonElement value = book.get(field);
        if (value != null && !value.isJsonNull() && !ids.get(target).contains(UUID.fromString(value.getAsString()).toString())) {
            throw new IllegalStateException("Книга " + book.get("id") + ": неизвестная ссылка " + field + "=" + value);
        }
    }
}
