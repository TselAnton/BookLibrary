package com.tsel.home.project.booklibrary.dao.repository.impl;

import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.dao.data.UserSettings;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Deprecated(since = "4.0")
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
