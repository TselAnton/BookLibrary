package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.schema.StorageEntity;
import lombok.*;

import java.io.Serial;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@StorageEntity(value = "genre", displayName = "Жанр", version = 1)
public class Genre implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = 2647731104385274297L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название")
    private String name;
}
