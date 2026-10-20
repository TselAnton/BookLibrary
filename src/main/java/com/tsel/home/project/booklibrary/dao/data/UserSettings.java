package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.schema.NullPolicy;
import com.tsel.home.project.booklibrary.dao.schema.StorageEntity;
import com.tsel.home.project.booklibrary.dao.schema.StorageField;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
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
@StorageEntity(value = "userSettings", displayName = "Настройки пользователя", version = 1)
public class UserSettings implements BaseEntity<String> {

    private static final String UNIQUE_KEY = "userSettings";

    @Serial
    private static final long serialVersionUID = -6130628202822480336L;

    @Property(value = "последняя выбранная папка с обложками")
    private String lastChosenCoverFolder;

    @Builder.Default
    @Property(value = "версии схем хранилища", nullable = false)
    @StorageField(defaultJson = "{}", onNull = NullPolicy.USE_DEFAULT)
    private Map<String, Integer> appliedSchemaVersions = new LinkedHashMap<>();

    public String getId() {
        return UNIQUE_KEY;
    }

    public void setId(String id) {
        // NOT NEEDED
    }
}
