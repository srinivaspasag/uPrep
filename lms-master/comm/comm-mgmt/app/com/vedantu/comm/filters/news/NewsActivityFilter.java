package com.vedantu.comm.filters.news;

import com.vedantu.commons.news.NewsActivity;

public interface NewsActivityFilter {

	public boolean accept( NewsActivity newsActivity );
}
