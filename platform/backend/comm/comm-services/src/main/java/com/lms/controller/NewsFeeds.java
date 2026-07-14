package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetNewsFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetActivityFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetOlderActivityFeedsReq;
import com.lms.services.NewsfeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/newsFeeds")
public class NewsFeeds {
    @Autowired
    private NewsfeedService newsfeedServiceImpl;

    @PostMapping("/getOlderActivityFeeds")
    public ResponseEntity<VedantuResponse> getOlderActivityFeeds(GetOlderActivityFeedsReq getOlderActivityFeedsReq) {
        return ResponseEntity.ok(newsfeedServiceImpl.getOlderActivityFeeds(getOlderActivityFeedsReq));
    }

    @PostMapping("/getNewsFeeds")
    public ResponseEntity<VedantuResponse> getNewsFeeds(GetNewsFeedsReq getNewsFeedsReq) {
        return ResponseEntity.ok(newsfeedServiceImpl.getNewsFeeds(getNewsFeedsReq));
    }

    @PostMapping("/getActivityFeeds")
    public ResponseEntity<VedantuResponse> getActivityFeeds(GetActivityFeedsReq getActivityFeedsReq) {
        return ResponseEntity.ok(newsfeedServiceImpl.getActivityFeeds(getActivityFeedsReq));
    }

}
