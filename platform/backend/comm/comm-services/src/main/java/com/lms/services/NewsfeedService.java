package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetNewsFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetActivityFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetOlderActivityFeedsReq;

public interface NewsfeedService {

    VedantuResponse getOlderActivityFeeds(GetOlderActivityFeedsReq getOlderActivityFeedsReq);

    VedantuResponse getNewsFeeds(GetNewsFeedsReq getNewsFeedsReq);

    VedantuResponse getActivityFeeds(GetActivityFeedsReq getActivityFeedsReq);
}
