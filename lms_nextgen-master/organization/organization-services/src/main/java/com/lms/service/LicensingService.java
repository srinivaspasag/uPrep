package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddLicensingPlanReq;
import com.lms.pojo.request.GetLicensingPlansReq;
import com.lms.pojo.request.MarkStateReq;
import com.lms.pojo.request.campaigns.DeleteLicensingPlanReq;

public interface LicensingService {
    VedantuResponse getSupportedFeatures();

    VedantuResponse getPlans(GetLicensingPlansReq getLicensingPlansReq);

    VedantuResponse create(AddLicensingPlanReq addLicensingPlanReq);

    VedantuResponse delete(DeleteLicensingPlanReq deleteLicensingPlanReq);

    VedantuResponse mark(MarkStateReq markStateReq);
}
