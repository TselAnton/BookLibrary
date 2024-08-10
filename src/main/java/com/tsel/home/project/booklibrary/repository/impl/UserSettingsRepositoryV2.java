package com.tsel.home.project.booklibrary.repository.impl;

import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.data.UserSettings;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@FileStorageName("userSettingsStorage.json")
public class UserSettingsRepositoryV2 extends AbstractFileRepositoryV2<String, UserSettings> {

    private static final String USER_SETTINGS_KEY = "userSettings";

    private static UserSettingsRepositoryV2 INSTANCE;

    public static UserSettingsRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserSettingsRepositoryV2();
        }
        return INSTANCE;
    }

    protected UserSettingsRepositoryV2() {
        super(UserSettings.class);
    }

    public void updateLastChosenCoverFile(File lastChosenCoverFile) {
        UserSettings userSettings = ofNullable(getByName(USER_SETTINGS_KEY)).orElse(new UserSettings());
        userSettings.setLastChosenCoverFolder(lastChosenCoverFile.getAbsoluteFile().getParent());
        save(userSettings);
    }

    public Optional<File> getLastChosenCoverFile() {
        return ofNullable(getByName(USER_SETTINGS_KEY))
                .map(UserSettings::getLastChosenCoverFolder)
                .filter(path -> Files.isDirectory(Path.of(path)))
                .map(File::new);
    }
}
