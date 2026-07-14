package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface DiscussionsService {
    VedantuResponse adddiscussion(AddDiscussionReq addDiscussionReq);

    VedantuResponse removediscussion(RemoveDiscussionReq removeDiscussionReq);

    VedantuResponse getdiscussionInfo(GetDiscussionReq getDiscussionReq);

    VedantuResponse getdiscussions(GetDiscussionsReq getDiscussionsReq);

    VedantuResponse fixdiscussions(GetDiscussionReq getDiscussionReq);

    VedantuResponse recordteacherResponse(RecordTeacherResponseReq recordTeacherResponseReq);

    VedantuResponse getSimilardiscussions(GetSimilarEntities getSimilarEntities);
}
