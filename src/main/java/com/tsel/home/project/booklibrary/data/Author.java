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
public class Author implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -7697277154251963838L;

    private String name;

    @Override
    public String getKey() {
        return name;
    }
}
