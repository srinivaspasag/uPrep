package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddMicrositeConfigReq;
import com.lms.pojo.request.GetOrgMicrositeConfigReq;
import com.lms.pojo.request.ValidateExternalURLReq;

public interface MicrositesService {
    VedantuResponse getConfiguRation(GetOrgMicrositeConfigReq getOrgMicrositeConfigReq);

    VedantuResponse addToConfig(AddMicrositeConfigReq addMicrositeConfigReq);

    VedantuResponse checkURL(ValidateExternalURLReq validateExternalURLReq);
}
