package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
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
@EntityDisplayName("Настройки пользователя")
public class UserSettings implements BaseEntity<String> {

    private static final String UNIQUE_KEY = "userSettings";

    @Serial
    private static final long serialVersionUID = -6130628202822480336L;

    @Property(value = "последняя выбранная папка с обложками")
    private String lastChosenCoverFolder;

    public String getId() {
        return UNIQUE_KEY;
    }

    public void setId(String id) {
        // NOT NEEDED
    }
}
