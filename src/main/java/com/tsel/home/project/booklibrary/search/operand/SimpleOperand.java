package com.tsel.home.project.booklibrary.search.operand;

public class SimpleOperand implements BaseOperand {

    private final String predicate;

    public SimpleOperand(String predicate) {
        this.predicate = predicate;
    }

    public String getPredicate() {
        return predicate;
    }
}
