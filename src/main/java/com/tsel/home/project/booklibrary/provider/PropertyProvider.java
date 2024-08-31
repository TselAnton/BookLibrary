package com.tsel.home.project.booklibrary.provider;

import static java.lang.String.format;

import com.tsel.home.project.booklibrary.utils.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyProvider {

    public static final Path PROPERTY_FILE_PATH = Paths.get(".properties");

    public static final String MAX_ARCHIVED_BACKUPS = "maxArchivedBackups";

    private static final String PROPERTY_SPLITERATOR = "=";
    private static final Map<String, String> DEFAULT_PROPERTIES;

    private final Path propertyFilePath;
    private final Map<String, String> properties;

    static {
        DEFAULT_PROPERTIES = new HashMap<>();
        DEFAULT_PROPERTIES.put(MAX_ARCHIVED_BACKUPS, "5");
    }

    public PropertyProvider(@Nullable Path rootPath) {
        this.propertyFilePath = FileUtils.resolvePath(rootPath, PROPERTY_FILE_PATH);
        if (FileUtils.isNotExists(propertyFilePath)) {
            initDefaultPropertiesFile();
        }
        this.properties = readProperties();
    }

    public String getProperty(String propertyName) {
        return this.properties.getOrDefault(propertyName, DEFAULT_PROPERTIES.get(propertyName));
    }

    private void initDefaultPropertiesFile() {
        FileUtils.createFile(propertyFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertyFilePath.toFile(), false))) {
            for (Entry<String, String> propertyEntry : DEFAULT_PROPERTIES.entrySet()) {
                writer.write(propertyEntry.getKey() + PROPERTY_SPLITERATOR + propertyEntry.getValue());
            }

        } catch (IOException e) {
            log.error("Exception while trying to init property file '{}'", propertyFilePath);
            throw new IllegalStateException(format("Проблема при создании property файла '%s'", propertyFilePath), e);
        }
    }

    private Map<String, String> readProperties() {
        Map<String, String> propertiesMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(propertyFilePath.toFile()))) {
            String propertyLine = reader.readLine();
            if (isCorrectProperty(propertyLine)) {
                String propertyKey = propertyLine.substring(0, propertyLine.indexOf(PROPERTY_SPLITERATOR));
                String propertyValue = propertyLine.substring(propertyLine.indexOf(PROPERTY_SPLITERATOR) + 1);

                propertiesMap.put(propertyKey, propertyValue);

            } else {
                log.warn("Skip property '{}' because it incorrect", propertyLine);
            }

        } catch (IOException e) {
            log.error("Exception while trying to read property file '{}'", propertyFilePath);
            throw new IllegalStateException(format("Проблема при создании property файла '%s'", propertyFilePath), e);
        }

        for (Entry<String, String> propertyEntry : DEFAULT_PROPERTIES.entrySet()) {
            propertiesMap.putIfAbsent(propertyEntry.getKey(), propertyEntry.getValue());
        }

        return propertiesMap;
    }

    private static boolean isCorrectProperty(String propertyLine) {
        return propertyLine.contains(PROPERTY_SPLITERATOR)
            && propertyLine.indexOf(PROPERTY_SPLITERATOR) != propertyLine.length() - 1;
    }
}
