package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Objects;

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
        Publisher publisher = (Publisher) o;
        return name.equals(publisher.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Publisher{" +
                "name='" + name + '\'' +
                '}';
    }
}
