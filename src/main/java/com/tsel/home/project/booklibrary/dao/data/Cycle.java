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
@EntityDisplayName("Цикл")
public class Cycle implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = 4292001364563617067L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название", nullable = false)
    private String name;

    @Property(value = "законченный цикл", nullable = false)
    private Boolean ended = false;

    @Property(value = "книг в цикле", nullable = false)
    private Integer booksInCycle = 1;

    @Override
    public String getKey() {
        return name;
    }
}
