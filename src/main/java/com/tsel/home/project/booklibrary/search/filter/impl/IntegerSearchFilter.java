package com.tsel.home.project.booklibrary.search.filter.impl;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import com.tsel.home.project.booklibrary.search.filter.AbstractNumericSearchFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IntegerSearchFilter extends AbstractNumericSearchFilter<Integer> {

    private static final Logger LOGGER = LogManager.getLogger(IntegerSearchFilter.class);

    @Override
    protected Class<Integer> getProcessedFilterValueClass() {
        return Integer.class;
    }

    @Override
    public boolean filter(Object value, String searchQuery, SearchFieldDefinition definition) {
        LOGGER.debug("Search by query '{}' in integer value '{}'", searchQuery, value);
        return super.filter(value, searchQuery, definition);
    }

    @Override
    protected Integer parseSearchValueAsNumber(String stringValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (Exception e) {
            LOGGER.warn("Exception while parsing integer '{}'", stringValue);
            return null;
        }
    }

    @Override
    protected boolean isLess(boolean withEqual, Integer fieldNumber, Integer searchNumber) {
        return withEqual
            ? fieldNumber <= searchNumber
            : fieldNumber < searchNumber;
    }

    @Override
    protected boolean isMore(boolean withEqual, Integer fieldNumber, Integer searchNumber) {
        return withEqual
            ? fieldNumber >= searchNumber
            : fieldNumber > searchNumber;
    }

    @Override
    protected boolean isEqual(boolean isNot, Integer fieldNumber, Integer searchNumber) {
        return isNot != fieldNumber.equals(searchNumber);
    }
}
