package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.requests.remarks.AddRemarksReq;
import com.lms.requests.remarks.GetRemarksReq;

public interface RemarksService {

    VedantuResponse addRemark(AddRemarksReq recordAttemptReq);

    VedantuResponse getRemarksForUser(GetRemarksReq getRemarksReq);

}
