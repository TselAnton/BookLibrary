package com.tsel.home.project.booklibrary.search.filter;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;

public interface SearchFilter {

    Class<?> getFilterValueClass();

    boolean filter(Object value, String searchQuery, SearchFieldDefinition definition);

    default String getTooltipInfo() {
        return "";
    }
}
