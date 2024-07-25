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
public class Publisher implements BaseEntity<String> {

    @Serial
    private static final long serialVersionUID = -3488297266687933995L;

    private String name;

    @Override
    public String getKey() {
        return name;
    }
}
