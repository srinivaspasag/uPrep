package com.lms.services;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface ContentService {

    VedantuResponse getContentLinks(GetContentsLinkReq getContentsLinkReq);

    VedantuResponse getContentresponse(GetContentReq getContentReq);

    VedantuResponse getRemovedContentLinks(GetContentsLinkReq getContentsLinkReq);

	VedantuResponse getContentDownloadLink(GetContentDownloadLinkReq getContentDownloadLinkReq);

	VedantuResponse getPdfDownloadLink( GetDownloadUrlOfPdfReq getDownloadUrlOfPdfReq);

	VedantuResponse getSecureLink(GetContentSecuredDownloadLinkReq getContentSecuredDownloadLinkReq);

	VedantuResponse getEntityInfoForApp(GetEntityInfoForAppReq getEntityInfoForAppReq);

	VedantuResponse validateResource(GetEntityReq getEntityReq);

	VedantuResponse getFileInfos(GetFileInfoReq getFileInfoReq);
    VedantuResponse getContentForDemo(GetContentForDemoReq getContentForDemoReq);

    VedantuResponse getcontents(GetContentsReq getContentsReq);

	VedantuResponse getEntityReviews(GetEntityReviewsReq getEntityReviewsReq);

	VedantuResponse addRatingAndFeedback(AddEntityInfoReq addEntityInfoReq);

	VedantuResponse getCMDSEntityInfo(GetEntityInfoForAppReq getEntityInfoForAppReq);
}
