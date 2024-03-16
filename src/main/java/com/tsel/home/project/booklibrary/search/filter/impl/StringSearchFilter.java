package com.tsel.home.project.booklibrary.search.filter.impl;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import com.tsel.home.project.booklibrary.search.filter.AbstractSearchFilter;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringSearchFilter extends AbstractSearchFilter<String> {

    private static final Logger LOGGER = LogManager.getLogger(StringSearchFilter.class);

    @Override
    protected Class<String> getProcessedFilterValueClass() {
        return String.class;
    }

    @Override
    protected boolean filterByQuery(String value, String searchQuery, SearchFieldDefinition definition) {
        LOGGER.debug("Search by query '{}' in value '{}'", searchQuery, value);
        return isNotBlank(value) && value.toLowerCase(Locale.ROOT).contains(searchQuery);
    }
}
