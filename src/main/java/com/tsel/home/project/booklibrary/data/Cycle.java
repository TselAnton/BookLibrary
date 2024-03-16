package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Cycle implements BaseEntity {

    @Serial
    private static final long serialVersionUID = 4292001364563617067L;

    private String name;
    private Boolean ended = false;
    private Integer booksInCycle = 1;

    @Override
    public String getKey() {
        return name;
    }
}
