package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.shared.Version;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppVersion {

    private static final Properties VALUES = load();

    private AppVersion() {}

    public static Version current() {
        return Version.parse(VALUES.getProperty("version"));
    }

    public static String platform() {
        return VALUES.getProperty("platform");
    }

    public static String helperResource() {
        return VALUES.getProperty("helper");
    }

    private static Properties load() {
        try (InputStream input = AppVersion.class.getResourceAsStream("/application-version.properties")) {
            if (input == null) {
                throw new IOException("Отсутствуют метаданные версии приложения.");
            }
            Properties values = new Properties();
            values.load(input);
            return values;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
