package com.tsel.home.project.booklibrary.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleDTO implements Serializable, ComboBoxDTO {

    @Serial
    private static final long serialVersionUID = 4166565914019052488L;

    private UUID id;
    private String name;

    public CycleDTO(String name) {
        this.id = null;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
