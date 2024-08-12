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
public class Publisher implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -3488297266687933995L;

    private UUID id;
    private String name;

    public Publisher(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    @Override
    public String getEntityPrintName() {
        return "Публицист";
    }

    @Override
    public String getKey() {
        return name;
    }
}
