package com.vedantu.comm.managers.news;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.news.NewsActivity;
 
public class FilterManager {
	Set<Filter> filters = new HashSet<Filter>();
	
	public boolean addFilter( Filter newActivityFilter ){
		return filters.add(newActivityFilter);
	}
	public boolean applyFilters ( NewsActivity newsActivity ){
		boolean passed = true;
		if( CollectionUtils.isNotEmpty(filters)){
		for( Filter filter : filters ){
			
			passed &= filter.accept(newsActivity);
		}
		}
		return passed;
	}
}
