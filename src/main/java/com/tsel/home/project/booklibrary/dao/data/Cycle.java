package com.tsel.home.project.booklibrary.dao.data;

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
public class Cycle implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = 4292001364563617067L;

    private UUID id;
    private String name;
    private Boolean ended = false;
    private Integer booksInCycle = 1;

    @Override
    public String getEntityPrintName() {
        return "Цикл";
    }

    @Override
    public String getKey() {
        return name;
    }
}
