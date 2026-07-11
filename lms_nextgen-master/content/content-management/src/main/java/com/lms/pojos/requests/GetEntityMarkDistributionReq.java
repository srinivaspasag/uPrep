package com.lms.pojos.requests;

import com.lms.pojos.requests.analytics.GetEntityResultAnalyticsReq;

import javax.validation.constraints.NotBlank;

public class GetEntityMarkDistributionReq extends GetEntityResultAnalyticsReq {

    @NotBlank(message = "bucketCount should not be null")
    public int bucketCount;
    public String brdId;// filter for only this board
}
