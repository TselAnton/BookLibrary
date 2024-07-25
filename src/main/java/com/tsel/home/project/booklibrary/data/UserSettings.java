package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserSettings implements BaseEntity<String> {

    @Serial
    private static final long serialVersionUID = -6130628202822480336L;

    private String lastChosenCoverFolder;

    @Override
    public String getKey() {
        return "userSettings";
    }
}
