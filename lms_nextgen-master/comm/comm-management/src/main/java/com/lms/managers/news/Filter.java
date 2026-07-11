package com.lms.managers.news;

import com.lms.common.news.NewsActivity;

public interface Filter {
    boolean accept(NewsActivity activity);

}
