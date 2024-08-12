package com.tsel.home.project.booklibrary.dao.data;

import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings implements BaseEntity<String> {

    private static final String UNIQUE_KEY = "userSettings";

    @Serial
    private static final long serialVersionUID = -6130628202822480336L;

    private String lastChosenCoverFolder;

    public String getId() {
        return UNIQUE_KEY;
    }

    public void setId(String id) {
        // NOT NEEDED
    }

    @Override
    public String getEntityPrintName() {
        return "Настройки пользователя";
    }

    @Override
    public String getKey() {
        return "userSettings";
    }
}
