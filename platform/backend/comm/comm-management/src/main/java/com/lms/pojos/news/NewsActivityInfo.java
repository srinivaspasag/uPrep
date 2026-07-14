package com.lms.pojos.news;

import com.lms.common.news.NewsActivity;

public class NewsActivityInfo extends NewsActivity {

    public String newsActivityId;

    public NewsActivityInfo() {
        super();
    }

    public NewsActivityInfo(NewsActivityInfo n) {
        super(n);
        this.newsActivityId = n.newsActivityId;
    }

    public NewsActivityInfo(NewsActivity n) {
        super(n);
        this.newsActivityId = "";
    }

}
