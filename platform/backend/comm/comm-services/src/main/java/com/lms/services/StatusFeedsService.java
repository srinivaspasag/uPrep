package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.requests.AddStatusFeedReq;
import com.lms.requests.DeleteStatusFeedReq;
import com.lms.requests.GetStatusFeedReq;

public interface StatusFeedsService {

    VedantuResponse addStatusFeed(AddStatusFeedReq addStatusFeedReq);

	VedantuResponse getStatusFeed(GetStatusFeedReq getStatusFeedReq);

	VedantuResponse delete(DeleteStatusFeedReq deleteStatusFeedReq);

}
