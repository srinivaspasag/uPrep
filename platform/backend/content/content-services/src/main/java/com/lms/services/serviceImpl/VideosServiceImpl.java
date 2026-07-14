package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.VideoManagerComponent;
import com.lms.enums.ExternalContentSrc;
import com.lms.interfaces.IDataCollector;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.GetPlaylistVideosRes;
import com.lms.pojos.responce.GetVideoRes;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.services.VideosService;
import com.lms.web.ExternalContentInfo;
import com.lms.web.VideoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VideosServiceImpl implements VideosService {
    private static final Logger logger = LoggerFactory.getLogger(VideosServiceImpl.class);
    @Autowired
    private VideoManagerComponent videoManagerComponent;

    protected static Map<String, String> getSessionParams() {
       /* return SessionExtractorUtils.getSessionParams(request().cookie(
                Play.application().configuration().getString("application.session.cookie")));*/
        return new HashMap<>();
    }

    @Override
    public VedantuResponse getVideoInfo(GetVideoInfoReq getInfoReq) {
        logger.info("requested video url :" + getInfoReq.url);
        IDataCollector dataCollector = null;
        ExternalContentSrc videoSrc = ExternalContentSrc.getSrc(getInfoReq.url);
        logger.info("videoSrcEnum : " + videoSrc);
        dataCollector = videoSrc.getDataCollector();
        ExternalContentInfo info = null;
        if (videoSrc == ExternalContentSrc.YouTube) {
            boolean isEmbeddable = dataCollector.isEmbeddable(getInfoReq.url);
            if (isEmbeddable) {
                // This youtube video can be embeddable
                info = dataCollector.getData(getInfoReq.url);
            } else {
                throw new VedantuException(VedantuErrorCode.CAN_NOT_EMBED, "This Video Url is Not Embeddable");
            }
        } else {
            info = dataCollector.getData(getInfoReq.url);
        }
        if (info instanceof VideoInfo) {
            logger.info("videoInfo object is : " + info);
            return new VedantuResponse(info);
        }
        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE, "url is not video url");

    }

    @Override
    public VedantuResponse getVideo(GetVideoReq request) {
        Map<String, String> sessionParamsMap = getSessionParams();
        sessionParamsMap.put("orgId", request.orgId);
        request.__setSessionParams(sessionParamsMap);

        GetVideoRes response = null;
        try {
            response = videoManagerComponent.getVideo(request);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }


    @Override
    public VedantuResponse getPlaylistVideos(GetPlaylistVideosReq request) throws VedantuException {
        Map<String, String> sessionParamsMap = getSessionParams();
        sessionParamsMap.put("orgId", request.orgId);
        request.__setSessionParams(sessionParamsMap);
        GetPlaylistVideosRes response = videoManagerComponent.getPlaylistVideos(request);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getVideos(GetVideosReq getVideosReq) {
        SearchListResponse<GetVideoRes> getVideosRes = null;
        try {
            getVideosRes = videoManagerComponent.getVideos(getVideosReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getVideosRes);
    }

    @Override
    public VedantuResponse getSimilarVideos(GetSimilarEntities getSimilarEntities) {
        ListResponse<GetVideoRes> response = videoManagerComponent.getSimilarVideos(getSimilarEntities);
        return new VedantuResponse(response);
    }
}
