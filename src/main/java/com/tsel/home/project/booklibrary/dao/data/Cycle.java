package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.schema.NullPolicy;
import com.tsel.home.project.booklibrary.dao.schema.StorageEntity;
import com.tsel.home.project.booklibrary.dao.schema.StorageField;
import java.io.Serial;
import java.util.UUID;
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
@StorageEntity(value = "cycle", displayName = "Цикл", version = 1)
public class Cycle implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = 4292001364563617067L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название", nullable = false)
    private String name;

    @Property(value = "законченный цикл", nullable = false)
    @StorageField(defaultJson = "false", onNull = NullPolicy.USE_DEFAULT)
    private Boolean ended = false;

    @Property(value = "книг в цикле", nullable = false)
    @StorageField(defaultJson = "1", onNull = NullPolicy.USE_DEFAULT)
    private Integer booksInCycle = 1;
}
