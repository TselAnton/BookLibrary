package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Objects;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public Integer getBooksInCycle() {
        return booksInCycle;
    }

    public void setBooksInCycle(Integer booksInCycle) {
        this.booksInCycle = booksInCycle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cycle cycle = (Cycle) o;
        return name.equals(cycle.name) && Objects.equals(ended, cycle.ended) && Objects.equals(booksInCycle, cycle.booksInCycle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ended, booksInCycle);
    }

    @Override
    public String toString() {
        return "Cycle{" +
                "name='" + name + '\'' +
                ", ended=" + ended +
                ", booksInCycle=" + booksInCycle +
                '}';
    }
}
