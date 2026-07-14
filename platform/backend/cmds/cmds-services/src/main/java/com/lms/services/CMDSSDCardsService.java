package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetSDCardReq;

public interface CMDSSDCardsService {
    VedantuResponse get(GetSDCardReq getSDCardReq);
}
