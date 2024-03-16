package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Objects;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Author implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -7697277154251963838L;

    private String name;

    public Author(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return name;
    }
}
