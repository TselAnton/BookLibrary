package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AudioBookSite implements BaseEntity {

    @Serial
    private static final long serialVersionUID = 1576946694915132507L;

    private UUID id = UUID.randomUUID();
    private String name;

    public AudioBookSite(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return id.toString();
    }
}
