package com.tsel.home.project.booklibrary.search.operand;

import lombok.Getter;

@Getter
public class SimpleOperand implements BaseOperand {

    private final String predicate;

    public SimpleOperand(String predicate) {
        this.predicate = predicate;
    }
}
