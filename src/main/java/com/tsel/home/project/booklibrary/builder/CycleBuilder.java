package com.tsel.home.project.booklibrary.builder;

import com.tsel.home.project.booklibrary.data.Cycle;

public final class CycleBuilder {

    private String name;
    private Boolean ended;
    private Integer booksInCycle;

    private CycleBuilder() {}

    public static CycleBuilder builder() {
        return new CycleBuilder();
    }

    public CycleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CycleBuilder ended(Boolean ended) {
        this.ended = ended;
        return this;
    }

    public CycleBuilder booksInCycle(Integer booksInCycle) {
        this.booksInCycle = booksInCycle;
        return this;
    }

    public Cycle build() {
        Cycle cycle = new Cycle();
        cycle.setName(name);
        cycle.setEnded(ended);
        cycle.setBooksInCycle(booksInCycle);
        return cycle;
    }
}
