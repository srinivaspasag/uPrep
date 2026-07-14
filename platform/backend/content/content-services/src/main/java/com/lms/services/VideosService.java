package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface VideosService {

    VedantuResponse getVideoInfo(GetVideoInfoReq getVideoInfoReq);

    VedantuResponse getVideo(GetVideoReq getVideoReq);

    VedantuResponse getPlaylistVideos(GetPlaylistVideosReq getPlaylistVideosReq);

    VedantuResponse getVideos(GetVideosReq getVideosReq);

    VedantuResponse getSimilarVideos(GetSimilarEntities getSimilarEntities);

}
