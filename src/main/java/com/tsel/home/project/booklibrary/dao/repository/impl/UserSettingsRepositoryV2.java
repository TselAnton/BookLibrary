package com.tsel.home.project.booklibrary.dao.repository.impl;

import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.UserSettings;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@FileStorageName("userSettingsStorage.json")
public class UserSettingsRepositoryV2 extends AbstractFileRepositoryV2<String, UserSettings> {

    private static final String USER_SETTINGS_KEY = "userSettings";

    public UserSettingsRepositoryV2(Path rootPath) {
        super(UserSettings.class, () -> USER_SETTINGS_KEY, rootPath);
    }

    public UserSettingsRepositoryV2() {
        super(UserSettings.class, () -> USER_SETTINGS_KEY, null);
    }

    public void updateLastChosenCoverFile(File lastChosenCoverFile) {
        UserSettings userSettings = ofNullable(getById(USER_SETTINGS_KEY)).orElse(new UserSettings());
        userSettings.setLastChosenCoverFolder(lastChosenCoverFile.getAbsoluteFile().getParent());
        save(userSettings);
    }

    public File getLastChosenCoverFile() {
        return ofNullable(getById(USER_SETTINGS_KEY))
            .map(UserSettings::getLastChosenCoverFolder)
            .filter(path -> Files.isDirectory(Path.of(path)))
            .map(File::new)
            .orElse(null);
    }
}
