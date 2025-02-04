package com.tsel.home.project.booklibrary.search;

import static java.util.stream.Collectors.toMap;

import com.tsel.home.project.booklibrary.search.filter.SearchFilter;
import com.tsel.home.project.booklibrary.search.filter.impl.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class SearchFilterFactory {

    private static final List<SearchFilter> SEARCH_FILTER_LIST = List.of(
        new CheckBoxSearchFilter(),
        new DoubleSearchFilter(),
        new IntegerSearchFilter(),
        new StringSearchFilter(),
        new BooleanSearchFilter()
    );

    private static final Map<Class<?>, SearchFilter> SEARCH_FILTER_MAP;

    static {
        SEARCH_FILTER_MAP = SEARCH_FILTER_LIST.stream()
            .collect(toMap(SearchFilter::getFilterValueClass, Function.identity()));
    }

    private SearchFilterFactory() {}

    public static SearchFilter getSearchFilter(Class<?> filterClass) {
        return SEARCH_FILTER_MAP.get(filterClass);
    }
}
