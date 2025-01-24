package com.tsel.home.project.booklibrary.dao.repository.impl;

import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.UserSettings;
import com.tsel.home.project.booklibrary.dao.identifier.StringIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepository;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@FileStorageName("userSettingsStorage.json")
public class UserSettingsRepository extends AbstractFileRepository<String, UserSettings> {

    private static final String USER_SETTINGS_KEY = "userSettings";

    public UserSettingsRepository(Path rootPath) {
        super(UserSettings.class, new StringIdentifierGenerator(USER_SETTINGS_KEY), rootPath);
    }

    public UserSettingsRepository() {
        super(UserSettings.class, new StringIdentifierGenerator(USER_SETTINGS_KEY), DEFAULT_REPOSITORY_PATH);
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
