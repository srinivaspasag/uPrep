package com.lms.managers.news;


import com.lms.common.news.NewsActivity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;


public class FilterManager {
    Set<Filter> filters = new HashSet<Filter>();

    public boolean addFilter(Filter newActivityFilter) {
        return filters.add(newActivityFilter);
    }

    public boolean applyFilters(NewsActivity newsActivity) {
        boolean passed = true;
        if (CollectionUtils.isNotEmpty(filters)) {
            for (Filter filter : filters) {

                passed &= filter.accept(newsActivity);
            }
        }
        return passed;
    }
}
