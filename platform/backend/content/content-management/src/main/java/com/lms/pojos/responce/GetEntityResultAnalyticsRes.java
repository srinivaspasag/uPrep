package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojos.TestMiniInfo;
import com.lms.pojos.responce.analytics.GetUserEntityResultAnalyticsRes;

public class GetEntityResultAnalyticsRes extends
        ListResponse<GetUserEntityResultAnalyticsRes> {

    public TestMiniInfo info;// test info

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{info:");
        builder.append(info);
        builder.append(", totalHits:");
        builder.append(totalHits);
        builder.append(", list:");
        builder.append(list);
        builder.append("}");
        return builder.toString();
    }

}