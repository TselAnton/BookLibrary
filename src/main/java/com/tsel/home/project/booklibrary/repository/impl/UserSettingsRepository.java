package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.UserSettings;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class UserSettingsRepository extends AbstractFileRepository<UserSettings> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-user-settings-storage.txt";
    private static final String USER_SETTINGS_KEY = "userSettings";

    private static UserSettingsRepository INSTANCE;

    public static UserSettingsRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserSettingsRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected UserSettingsRepository(String storageFileName) {
        super(storageFileName);
    }

    public void updateLastChosenCoverFile(File lastChosenCoverFile) {
        UserSettings userSettings = ofNullable(getByKey(USER_SETTINGS_KEY)).orElse(new UserSettings());
        userSettings.setLastChosenCoverFolder(lastChosenCoverFile.getAbsoluteFile().getParent());
        save(userSettings);
    }

    public Optional<File> getLastChosenCoverFile() {
        return ofNullable(getByKey(USER_SETTINGS_KEY))
                .map(UserSettings::getLastChosenCoverFolder)
                .filter(path -> Files.isDirectory(Path.of(path)))
                .map(File::new);
    }
}
