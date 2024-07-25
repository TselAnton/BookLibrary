package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AudioBookSite implements BaseEntity<String> {

    @Serial
    private static final long serialVersionUID = 1576946694915132507L;

    private String name;

    @Override
    public String getKey() {
        return name;
    }
}
