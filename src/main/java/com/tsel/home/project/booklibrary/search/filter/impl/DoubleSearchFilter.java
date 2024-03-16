package com.tsel.home.project.booklibrary.search.filter.impl;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import com.tsel.home.project.booklibrary.search.filter.AbstractNumericSearchFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DoubleSearchFilter extends AbstractNumericSearchFilter<Double> {

    private static final Logger LOGGER = LogManager.getLogger(DoubleSearchFilter.class);

    @Override
    protected Class<Double> getProcessedFilterValueClass() {
        return Double.class;
    }

    @Override
    public boolean filter(Object value, String searchQuery, SearchFieldDefinition definition) {
        LOGGER.debug("Search by query '{}' in double value '{}'", searchQuery, value);
        return super.filter(value, searchQuery, definition);
    }

    @Override
    protected Double parseSearchValueAsNumber(String stringValue) {
        try {
            return Double.parseDouble(stringValue);
        } catch (Exception e) {
            LOGGER.warn("Exception while parsing double '{}'", stringValue);
            return null;
        }
    }

    @Override
    protected boolean isLess(boolean withEqual, Double fieldNumber, Double searchNumber) {
        return withEqual
            ? fieldNumber <= searchNumber
            : fieldNumber < searchNumber;
    }

    @Override
    protected boolean isMore(boolean withEqual, Double fieldNumber, Double searchNumber) {
        return withEqual
            ? fieldNumber >= searchNumber
            : fieldNumber > searchNumber;
    }

    @Override
    protected boolean isEqual(boolean isNot, Double fieldNumber, Double searchNumber) {
        return isNot != fieldNumber.equals(searchNumber);
    }
}
