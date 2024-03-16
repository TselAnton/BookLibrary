package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Publisher implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -3488297266687933995L;

    private String name;

    public Publisher(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return name;
    }
}
