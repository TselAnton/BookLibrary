package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
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
@EntityDisplayName("Автор")
public class Author implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -7697277154251963838L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "имя", nullable = false)
    private String name;

    @Deprecated(since = "4.0")
    public Author(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    @Override
    public String getKey() {
        return name;
    }
}
