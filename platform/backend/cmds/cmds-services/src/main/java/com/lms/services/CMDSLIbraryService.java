package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface CMDSLIbraryService {
    VedantuResponse addToLibrary(AddToLibraryReq addToLibraryReq);

    VedantuResponse removeFromLibrary(AddToLibraryReq addToLibraryReq);

    VedantuResponse getLibraryResources(GetLibraryResourcesReq getLibraryResourcesReq);

    VedantuResponse makeVisible(MakeVisibleReq makeVisibleReq);

    VedantuResponse getVisibilityStatus(GetVisibilityChartReq getVisibilityChartReq);

    VedantuResponse move(UpdateRankReq updateRankReq);
}
