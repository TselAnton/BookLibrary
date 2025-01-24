package com.tsel.home.project.booklibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO implements Serializable, ComboBoxDTO {

    @Serial
    private static final long serialVersionUID = -8773074344216491262L;

    private UUID id;
    private String name;

    public GenreDTO(String name) {
        this.id = null;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
