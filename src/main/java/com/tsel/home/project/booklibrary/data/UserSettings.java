package com.tsel.home.project.booklibrary.data;

import java.io.File;
import java.io.Serial;
import java.util.Objects;

public class UserSettings implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -6130628202822480336L;

    private String lastChosenCoverFolder;

    public UserSettings() {}

    @Override
    public String getKey() {
        return "userSettings";
    }

    public String getLastChosenCoverFolder() {
        return lastChosenCoverFolder;
    }

    public void setLastChosenCoverFolder(String lastChosenCoverFolder) {
        this.lastChosenCoverFolder = lastChosenCoverFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSettings that = (UserSettings) o;
        return Objects.equals(lastChosenCoverFolder, that.lastChosenCoverFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastChosenCoverFolder);
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "lastPathToCover='" + lastChosenCoverFolder + '\'' +
                '}';
    }
}
