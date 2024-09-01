package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity implements BaseEntity<String> {

    private String id;
    private String string;
    private Integer integer;

    public TestEntity(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
        this.id = UUID.randomUUID().toString();
    }
}
