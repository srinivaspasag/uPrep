package com.vedantu.comm.managers.news;

import com.vedantu.commons.news.NewsActivity;

public interface Filter {
	boolean accept( NewsActivity activity );
}
