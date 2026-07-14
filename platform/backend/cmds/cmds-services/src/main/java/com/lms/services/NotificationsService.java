package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetRegIdsReq;
import com.lms.pojos.requests.NotificationRegIDReq;

public interface NotificationsService {
    VedantuResponse getRegIds(GetRegIdsReq getRegIdsReq);

    VedantuResponse registerById(NotificationRegIDReq NotificationRegIDReq);
}
