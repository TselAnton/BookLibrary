package com.tsel.home.project.booklibrary.search.operand;

import java.util.ArrayList;
import java.util.List;

public class CompositeOperand implements BaseOperand {

    private final List<BaseOperand> operands = new ArrayList<>();
    private final OperatorType operator;

    public CompositeOperand(OperatorType operator) {
        this.operator = operator;
    }

    public List<BaseOperand> getOperands() {
        return operands;
    }

    public OperatorType getOperator() {
        return operator;
    }
}
