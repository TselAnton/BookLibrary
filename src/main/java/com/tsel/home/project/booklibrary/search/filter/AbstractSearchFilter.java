package com.tsel.home.project.booklibrary.search.filter;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;

public abstract class AbstractSearchFilter<T> implements SearchFilter {

    @Override
    public Class<?> getFilterValueClass() {
        return getProcessedFilterValueClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean filter(Object value, String searchQuery, SearchFieldDefinition definition) {
        return filterByQuery((T) value, searchQuery, definition);
    }

    protected abstract Class<T> getProcessedFilterValueClass();

    protected abstract boolean filterByQuery(T value, String searchQuery, SearchFieldDefinition definition);
}
