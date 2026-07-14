package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddCommentReq;
import com.lms.pojos.requests.GetCommentReq;
import com.lms.pojos.requests.GetCommentsReq;

public interface CommentService {
    VedantuResponse addcomment(AddCommentReq addCommentReq);

    VedantuResponse getcomment(GetCommentReq getCommentReq);

    VedantuResponse getcomments(GetCommentsReq getCommentsReq);
}
