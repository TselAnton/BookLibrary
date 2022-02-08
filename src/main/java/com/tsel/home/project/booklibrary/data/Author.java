package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Objects;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return name.equals(author.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                '}';
    }
}
