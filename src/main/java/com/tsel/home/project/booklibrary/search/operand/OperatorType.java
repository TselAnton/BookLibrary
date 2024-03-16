package com.tsel.home.project.booklibrary.search.operand;

import java.util.Arrays;
import java.util.List;

public enum OperatorType {
    AND("AND", "И"),
    OR("OR", "ИЛИ");

    private final List<String> names;

    OperatorType(String... names) {
        this.names = Arrays.asList(names);
    }

    public List<String> getNames() {
        return names;
    }

    public static OperatorType getOperator(String operatorString) {
        return Arrays.stream(OperatorType.values())
            .filter(operator -> operator.names.contains(operatorString))
            .findFirst()
            .orElse(null);
    }
}
