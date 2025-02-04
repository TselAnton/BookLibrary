package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import lombok.*;

import java.io.Serial;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityDisplayName("Жанр")
public class Genre implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = 2647731104385274297L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название")
    private String name;
}
