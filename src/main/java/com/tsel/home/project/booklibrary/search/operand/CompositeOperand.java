package com.tsel.home.project.booklibrary.search.operand;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CompositeOperand implements BaseOperand {

    private final List<BaseOperand> operands = new ArrayList<>();
    private final OperatorType operator;

    public CompositeOperand(OperatorType operator) {
        this.operator = operator;
    }
}
