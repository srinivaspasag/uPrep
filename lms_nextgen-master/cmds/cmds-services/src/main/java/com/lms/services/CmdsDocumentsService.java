package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetCMDSDocumentReq;

public interface CmdsDocumentsService {

    VedantuResponse reconvert(GetCMDSDocumentReq getCMDSDocumentReq);

    VedantuResponse get(GetCMDSDocumentReq getCMDSDocumentReq);
}
