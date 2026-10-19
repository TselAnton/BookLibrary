package com.tsel.home.project.booklibrary.filter.processor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.filter.condition.NumericCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NumericFilterProcessorTest {

    private final NumericFilterProcessor processor = new NumericFilterProcessor();

    @ParameterizedTest
    @CsvSource({
        "EQUALS, false, true, false",
        "NOT_EQUALS, true, false, true",
        "GREATER, false, false, true",
        "AT_LEAST, false, true, true",
        "LESS, true, false, false",
        "AT_MOST, true, true, false"
    })
    void testNumericComparisonsPreserveBoundariesAndExcludeMissingValues(
        NumericOperation operation, boolean below, boolean equal, boolean above
    ) {
        NumericCondition condition = processor.condition(operation, "0,00", "");
        assertEquals(below, condition.matches(processor.normalize(-1.0)));
        assertEquals(equal, condition.matches(processor.normalize(0.0)));
        assertEquals(above, condition.matches(processor.normalize(1.0)));
        assertFalse(condition.matches(processor.normalize(null)));
        assertFalse(condition.matches(MissingValue.NOT_APPLICABLE));
    }

    @Test
    void testExactDecimalMatchingDoesNotMatchAPartialPrice() {
        Object price = processor.normalize(555.555);
        assertTrue(processor.condition(NumericOperation.EQUALS, "555,555", "").matches(price));
        assertFalse(processor.condition(NumericOperation.EQUALS, "555", "").matches(price));
    }
}
