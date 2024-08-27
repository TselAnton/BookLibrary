package com.tsel.home.project.booklibrary.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AudioBookSiteDTO {

    private UUID id;

    private String name;

    @Override
    public String toString() {
        return name;
    }
}
