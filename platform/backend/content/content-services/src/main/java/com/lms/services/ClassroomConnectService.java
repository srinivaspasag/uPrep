package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddScheduleReq;
import com.lms.pojos.requests.GetScheduleReq;
import com.lms.pojos.requests.RemoveScheduleReq;

public interface ClassroomConnectService {
    VedantuResponse getschedule(GetScheduleReq getScheduleReq);

    VedantuResponse removedaySchedule(GetScheduleReq getScheduleReq);

    VedantuResponse removeschedule(RemoveScheduleReq removeScheduleReq);

    VedantuResponse getDayschedule(GetScheduleReq getScheduleReq);

    VedantuResponse addschedule(AddScheduleReq addScheduleReq);
}
