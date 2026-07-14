package com.lms.pojos.requests;

import com.lms.pojos.requests.analytics.GetEntityResultAnalyticsReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetEntityLeaderBoardReq extends GetEntityResultAnalyticsReq {

    public boolean miniInfo;
}
