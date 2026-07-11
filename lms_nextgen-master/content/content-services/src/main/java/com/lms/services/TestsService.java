package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetTestsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.requests.tests.GetTestInfoReq;

public interface TestsService {
    VedantuResponse getTestInfo(GetTestInfoReq getTestInfoReq);

    VedantuResponse getTests(GetTestsReq getTestsReq);

    VedantuResponse getTestQuestions(GetTestDetailsReq getTestDetailsReq);
}
