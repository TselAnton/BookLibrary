package com.tsel.home.project.booklibrary.search.filter;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractNumericSearchFilter<T extends Number> extends AbstractSearchFilter<T> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractNumericSearchFilter.class);

    protected static final String LESS = "<";
    protected static final String LESS_OR_EQUAL = "<=";
    protected static final String MORE = ">";
    protected static final String MORE_OR_EQUAL = ">=";
    protected static final String EQUAL = "=";
    protected static final String NOT_EQUAL = "!=";
    protected static final String CLOSE = "~=";

    @Override
    protected boolean filterByQuery(T value, String searchQuery, SearchFieldDefinition definition) {
        T preparedValue = value != null ? value : parseSearchValueAsNumber("0");
        boolean isMatchedByAliases = definition.aliases()
            .stream()
            .filter(searchQuery::startsWith)
            .anyMatch(alias -> filterByNumber(preparedValue, searchQuery, alias));

        return isMatchedByAliases || String.valueOf(value).contains(searchQuery);
    }

    @Override
    public String getTooltipInfo() {
        return "возможные условия: >, >=, <, <=, =, != и ~= для поиска подстроки в числе";
    }

    protected abstract T parseSearchValueAsNumber(String stringValue);

    protected abstract boolean isLess(boolean withEqual, T fieldNumber, T searchNumber);

    protected abstract boolean isMore(boolean withEqual, T fieldNumber, T searchNumber);

    protected abstract boolean isEqual(boolean isNot, T fieldNumber, T searchNumber);

    private boolean filterByNumber(T fieldNumber, String searchQuery, String keyName) {
        String searchParams = searchQuery.substring(keyName.length()).trim();
        if (searchParams.startsWith(LESS)) {
            return filterByLess(fieldNumber, searchParams);
        }
        if (searchParams.startsWith(MORE)) {
            return filterByMore(fieldNumber, searchParams);
        }
        if (searchParams.contains(EQUAL)) {
            return filterByEqual(fieldNumber, searchParams);
        }
        return false;
    }

    private boolean filterByLess(T fieldNumber, String searchParams) {
        boolean isLessOrEqual = searchParams.startsWith(LESS_OR_EQUAL);
        String searchNumberString = searchParams.substring(isLessOrEqual ? LESS_OR_EQUAL.length() : LESS.length()).trim();
        T searchNumber = parseSearchValueAsNumber(searchNumberString);
        return searchNumber != null && isLess(isLessOrEqual, fieldNumber, searchNumber);
    }

    private boolean filterByMore(T fieldNumber, String searchParams) {
        boolean isMoreOrEqual = searchParams.startsWith(MORE_OR_EQUAL);
        String searchNumberString = searchParams.substring(isMoreOrEqual ? MORE_OR_EQUAL.length() : MORE.length()).trim();
        T searchNumber = parseSearchValueAsNumber(searchNumberString);
        return searchNumber != null && isMore(isMoreOrEqual, fieldNumber, searchNumber);
    }

    private boolean filterByEqual(T fieldNumber, String searchParams) {
        if (searchParams.startsWith(CLOSE)) {
            return filterByCloseValue(fieldNumber, searchParams);
        }

        boolean isNotEqual = searchParams.startsWith(NOT_EQUAL);
        String searchNumberString = searchParams.substring(isNotEqual ? NOT_EQUAL.length() : EQUAL.length()).trim();
        T searchNumber = parseSearchValueAsNumber(searchNumberString);
        return searchNumber != null && isEqual(isNotEqual, fieldNumber, searchNumber);
    }

    private boolean filterByCloseValue(T fieldNumber, String searchParams) {
        String searchNumberString = searchParams.substring(CLOSE.length()).trim();
        String fieldNumberString = convertToStringSafely(fieldNumber);
        return fieldNumberString != null && fieldNumberString.contains(searchNumberString);
    }

    private String convertToStringSafely(T fieldNumber) {
        try {
            return String.valueOf(fieldNumber);
        } catch (Exception e) {
            LOGGER.warn("Exception while convert fieldNumber {} to string", fieldNumber, e);
            return null;
        }
    }
}
