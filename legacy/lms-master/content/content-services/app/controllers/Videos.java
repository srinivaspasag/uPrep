package controllers;

import java.util.Map;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.VideoManager;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.GetVideoInfoReq;
import com.vedantu.content.pojos.requests.videos.GetPlaylistVideosReq;
import com.vedantu.content.pojos.requests.videos.GetVideoReq;
import com.vedantu.content.pojos.requests.videos.GetVideosReq;
import com.vedantu.content.pojos.responses.videos.GetPlaylistVideosRes;
import com.vedantu.content.pojos.responses.videos.GetVideoRes;
import com.vedantu.web.datacollector.ExternalContentInfo;
import com.vedantu.web.datacollector.IDataCollector;
import com.vedantu.web.datacollector.VideoInfo;
import com.vedantu.web.enums.ExternalContentSrc;

public class Videos extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Videos.class);

    /**
     * @return data to ui from youtube/vimeo/ or any other url
     */
    public static Result getVideoInfo() {

        Form<GetVideoInfoReq> getInfoForm = Form.form(GetVideoInfoReq.class).bindFromRequest();
        if (getInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getInfoForm))).toObjectNode());
        }
        GetVideoInfoReq getInfoReq = getInfoForm.get();
        LOGGER.info("requested video url :" + getInfoReq.url);
        IDataCollector dataCollector = null;
        ExternalContentSrc videoSrc = ExternalContentSrc.getSrc(getInfoReq.url);
        LOGGER.info("videoSrcEnum : " + videoSrc);
        dataCollector = videoSrc.getDataCollector();
        ExternalContentInfo info = null;
        if(videoSrc == ExternalContentSrc.YouTube){
            boolean isEmbeddable = dataCollector.isEmbeddable(getInfoReq.url);
            if(isEmbeddable){
                // This youtube video can be embeddable
                info = dataCollector.getData(getInfoReq.url);
            }else{
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.CAN_NOT_EMBED,
                                "This Video Url is Not Embeddable")).toObjectNode());
            }
        }else{
            info = dataCollector.getData(getInfoReq.url);
        }
        if (info instanceof VideoInfo) {
            LOGGER.info("videoInfo object is : " + info);
            return ok(getResultResponse(info).toObjectNode());
        }
        return ok(getErrorResponse(
                new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                        "url is not video url")).toObjectNode());
    }

    public static Result getVideo() {

        Form<GetVideoReq> requestForm = Form.form(GetVideoReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        Map<String, String> sessionParamsMap = getSessionParams();
        GetVideoReq request = requestForm.get();
        sessionParamsMap.put("orgId", request.orgId);
        request.__setSessionParams(sessionParamsMap);

        GetVideoRes response = null;
        try {
            response = VideoManager.INSTANCE.getVideo(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getVideos() {

        Form<GetVideosReq> requestForm = Form.form(GetVideosReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetVideosReq request = requestForm.get();
        SearchListResponse<GetVideoRes> getVideosRes = null;
        try {
            getVideosRes = VideoManager.INSTANCE.getVideos(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getVideosRes).toObjectNode());
    }

    public static Result getSimilarVideos() {

        Form<GetSimilarEntities> requestForm = Form.form(GetSimilarEntities.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, requestForm.errors()
                            .toString())).toObjectNode());
        }
        GetSimilarEntities request = requestForm.get();
        ListResponse<GetVideoRes> response = VideoManager.INSTANCE.getSimilarVideos(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getPlaylistVideos(){
        Form<GetPlaylistVideosReq> requestForm = Form.form(GetPlaylistVideosReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, requestForm.errors()
                            .toString())).toObjectNode());
        }
        GetPlaylistVideosReq request = requestForm.get();
        Map<String, String> sessionParamsMap = getSessionParams();
        sessionParamsMap.put("orgId", request.orgId);
        request.__setSessionParams(sessionParamsMap);
        GetPlaylistVideosRes response = VideoManager.INSTANCE.getPlaylistVideos(request);
        return ok(getResultResponse(response).toObjectNode());
    }

}
