package com.tsel.home.project.booklibrary.search.filter.impl;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import com.tsel.home.project.booklibrary.search.filter.AbstractSearchFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BooleanSearchFilter extends AbstractSearchFilter<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(BooleanSearchFilter.class);

    private static final String REVERSE = "!";

    @Override
    protected Class<Boolean> getProcessedFilterValueClass() {
        return Boolean.class;
    }

    @Override
    public String getTooltipInfo() {
        return "для отрицания в начале поставить \"!\"";
    }

    @Override
    protected boolean filterByQuery(Boolean booleanValue, String searchQuery, SearchFieldDefinition definition) {
        LOGGER.debug("Search by query '{}' in value '{}'", searchQuery, booleanValue);
        return booleanValue != null && definition.aliases()
            .stream()
            .filter(searchQuery::contains)
            .anyMatch(alias -> filterByCheckBox(booleanValue, searchQuery, alias));

    }

    private static boolean filterByCheckBox(Boolean booleanValue, String searchQuery, String keyName) {
        if (searchQuery.startsWith(REVERSE) && keyName.equals(searchQuery.substring(1).trim())) {
            return !Boolean.TRUE.equals(booleanValue);
        } else {
            return keyName.equals(searchQuery) && Boolean.TRUE.equals(booleanValue);
        }
    }
}
