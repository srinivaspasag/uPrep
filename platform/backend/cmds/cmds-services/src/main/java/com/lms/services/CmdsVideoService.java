package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetCMDSVideoReq;

public interface CmdsVideoService {

    VedantuResponse getVideo(GetCMDSVideoReq getCMDSVideoReq);

    VedantuResponse reporocess(String id);

    VedantuResponse convertAgain(String id);
}
