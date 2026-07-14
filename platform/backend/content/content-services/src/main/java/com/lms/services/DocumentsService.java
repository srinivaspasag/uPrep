package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetDocumentReq;
import com.lms.pojos.requests.GetDocumentsReq;
import com.lms.pojos.requests.GetSimilarEntities;

public interface DocumentsService {
    VedantuResponse getdocument(GetDocumentReq getDocumentReq);

    VedantuResponse getdocuments(GetDocumentsReq getDocumentsReq);

    VedantuResponse getsimilarDocuments(GetSimilarEntities getSimilarEntities);
}
