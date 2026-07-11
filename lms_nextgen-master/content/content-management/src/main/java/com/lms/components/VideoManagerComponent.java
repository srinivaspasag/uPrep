package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType.LinkType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.ModuleEntry;
import com.lms.models.Organization;
import com.lms.models.Video;
import com.lms.pojos.requests.GetPlaylistVideosReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.GetVideoReq;
import com.lms.pojos.requests.GetVideosReq;
import com.lms.pojos.responce.GetPlaylistVideosRes;
import com.lms.pojos.responce.GetVideoRes;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import com.lms.pojos.search.details.VideoSearchIndexDetails;
import com.lms.repository.ModuleRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.repository.VideoRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class VideoManagerComponent extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(VideoManagerComponent.class);
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private ModuleRepo moduleRepo;

    public GetVideoRes getVideo(GetVideoReq request) {
        Optional<Video> videoOptional = videoRepo.findById(request.id);
        if (!videoOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND);
        }
        Video video = videoOptional.get();
        if (video.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetVideoRes videoRes = new GetVideoRes();
        videoRes.fromMongoModel(video);
        // TODO decorate video thumnail url
        // TODO update streaming video url
        logger.info("videoInfo: " + videoRes);
        videoRes = (GetVideoRes) annotateExtraInfo(request.userId, video.contentSrc != null
                        && video.contentSrc.type == EntityType.ORGANIZATION ? video.contentSrc.id : null,
                EntityType.VIDEO, videoRes);

        annotateVideoURLInfo(videoRes, request.isWebReq(), request.__getSessionParams(), request.orgId);
        return videoRes;

    }

    protected void annotateVideoURLInfo(VideoSearchIndexDetails video, boolean isWebReq,
                                        Map<String, String> sessionParams, String orgId) {
        annotateLinkInfo(video);
        if (video.linkType == LinkType.UPLOADED) {
            String[] originalExtension = (video.originalFileName).split("\\.");
            String extension = originalExtension[originalExtension.length - 1];
            logger.debug("backup url extension is " + extension);
            video.url = (video.converted) ? getEntityVideoSecureURL(
                    EntityType.VIDEO, video.uuid, sessionParams, isWebReq) :
                    getEntityVideoSecureURL(EntityType.VIDEO, video.uuid, extension,
                            FileCategory.ORIGINAL, sessionParams, isWebReq);
            video.backupVideoUrl = getEntityVideoSecureURL(EntityType.VIDEO,
                    video.uuid, extension, FileCategory.ORIGINAL, sessionParams, isWebReq);
            Optional<Organization> orgOptional = organizationRepo.findById(orgId);
            Organization org = orgOptional.get();
            if (org.disableDownload) {
                video.s3url = (video.converted) ? getEntityVideoS3URL(
                        EntityType.VIDEO, video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                        FileCategory.CONVERTED) : getEntityVideoS3URL(
                        EntityType.VIDEO, video.uuid, extension, FileCategory.ORIGINAL);
                video.s3HDurl = getEntityVideoS3URL(EntityType.VIDEO, video.uuid,
                        extension, FileCategory.ORIGINAL);
            } else {
                video.s3url = video.url;
                video.s3HDurl = video.backupVideoUrl;
            }
            video.disableDownload = org.disableDownload;
        }

        if (!StringUtils.isEmpty(video.thumbnail)) {
            video.poster = getEntityPoster(EntityType.VIDEO,
                    video.thumbnail);
            video.thumbnail = getEntityThumbnail(EntityType.VIDEO,
                    video.thumbnail);
        } else {
            video.thumbnail = "";
            video.poster = "";
        }

    }


    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    public GetPlaylistVideosRes getPlaylistVideos(GetPlaylistVideosReq request) throws VedantuException {
        GetPlaylistVideosRes response = new GetPlaylistVideosRes();
        Optional<com.lms.models.Module> moduleOptional = moduleRepo.findById(request.moduleId);
        com.lms.models.Module module = moduleOptional.get();
        SrcEntity entity = new SrcEntity(EntityType.VIDEO, request.id);
        ModuleEntry entry = new ModuleEntry();
        entry.entity = entity;
        int index = module.children.indexOf(entry);
        for (int i = index + 1; i < module.children.size(); i++) {
            if (response.videoIds.size() == 4) {
                break;
            }
            if (module.children.get(i).name == null && module.children.get(i).entity.type == EntityType.VIDEO) {
                String videoId = module.children.get(i).entity.id;
                GetVideoReq getVideoReq = new GetVideoReq();
                getVideoReq.callingApp = request.callingApp;
                getVideoReq.callingAppId = request.callingAppId;
                getVideoReq.id = videoId;
                getVideoReq.orgId = request.orgId;
                getVideoReq.userId = request.userId;
                getVideoReq.callingUserId = request.callingUserId;
                getVideoReq.__setSessionParams(request.__getSessionParams());
                GetVideoRes getVideoRes = new GetVideoRes();
                try {
                    if (getVideoReq.id == null) {
                        throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND);
                    }
                    getVideoRes = getVideo(getVideoReq);
                } catch (VedantuException e) {
                    logger.error("Problem in getting video response");
                    e.printStackTrace();
                }
                response.videoIds.add(getVideoRes);
            }
        }
        return response;

    }

    public SearchListResponse<GetVideoRes> getVideos(GetVideosReq request) {
        SearchListResponse<GetVideoRes> results = getEntityInfos(request, EntityType.VIDEO,
                GetVideoRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.VIDEO, results.list);
        annotateVideoURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }

    private void annotateVideoURLInfo(List<? extends VideoSearchIndexDetails> videoList,
                                      boolean isWebReq, String orgId) {
        for (VideoSearchIndexDetails video : videoList) {
            annotateVideoURLInfo(video, isWebReq, orgId);
        }
    }

    protected void annotateVideoURLInfo(VideoSearchIndexDetails video, boolean isWebReq, String orgId) {
        annotateVideoURLInfo(video, isWebReq, null, orgId);
    }

    public ListResponse<GetVideoRes> getSimilarVideos(GetSimilarEntities request) {
        ListResponse<GetVideoRes> results = getSimilarEntityInfos(request, GetVideoRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.VIDEO, results.list);
        annotateVideoURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }


}
