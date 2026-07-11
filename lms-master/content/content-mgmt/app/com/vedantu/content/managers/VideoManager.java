package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.models.Document;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.Video;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.videos.GetPlaylistVideosReq;
import com.vedantu.content.pojos.requests.videos.GetVideoReq;
import com.vedantu.content.pojos.requests.videos.GetVideosReq;
import com.vedantu.content.pojos.responses.videos.GetPlaylistVideosRes;
import com.vedantu.content.pojos.responses.videos.GetVideoRes;
import com.vedantu.content.search.details.VideoSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;

public class VideoManager extends AbstractContentManager {

    private static final ALogger LOGGER   = Logger.of(VideoManager.class);
    public static VideoManager   INSTANCE = new VideoManager();

    static {

    }

    public VideoManager() {

    }

    // public boolean deleteVideo(VideoDeleteReq request) throws VedantuException {
    // Video video = VideoDAO.INSTANCE.getById(request.id);
    // if (video == null) {
    // throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND);
    // }
    // else if(video.recordState == VedantuRecordState.TEMPORARY)
    // {
    // throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
    // }
    // else if(video.published == true)
    // {
    // throw new VedantuException(VedantuErrorCode.CONTENT_ALREADY_PUBLISHED);
    // }
    //
    // else if(video. == true)
    // {
    // throw new VedantuException(VedantuErrorCode.CONTENT_ALREADY_PUBLISHED);
    // }
    //
    // GetVideoRes videoRes = new GetVideoRes();
    // videoRes.fromMongoModel(video);
    // // TODO decorate video thumnail url
    // // TODO update streaming video url
    // LOGGER.info("videoInfo: " + videoRes);
    // videoRes = (GetVideoRes) annotateExtraInfo(request.userId, video.contentSrc != null
    // && video.contentSrc.type == EntityType.ORGANIZATION ? video.contentSrc.id : null,
    // EntityType.VIDEO, videoRes);
    //
    // annotateVideoURLInfo(videoRes);
    // return videoRes;
    // return false;
    // }

    public GetVideoRes getVideo(GetVideoReq request) throws VedantuException {

        Video video = VideoDAO.INSTANCE.getById(request.id);
        if (video == null) {
            throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND);
        }
        if (video.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetVideoRes videoRes = new GetVideoRes();
        videoRes.fromMongoModel(video);
        // TODO decorate video thumnail url
        // TODO update streaming video url
        LOGGER.info("videoInfo: " + videoRes);
        videoRes = (GetVideoRes) annotateExtraInfo(request.userId, video.contentSrc != null
                && video.contentSrc.type == EntityType.ORGANIZATION ? video.contentSrc.id : null,
                EntityType.VIDEO, videoRes);

        annotateVideoURLInfo(videoRes, request.isWebReq(), request.__getSessionParams(), request.orgId);
        return videoRes;
    }

    public SearchListResponse<GetVideoRes> getVideos(GetVideosReq request) throws VedantuException {

        SearchListResponse<GetVideoRes> results = getEntityInfos(request, EntityType.VIDEO,
                GetVideoRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.VIDEO, results.list);
        annotateVideoURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }

    protected void annotateVideoURLInfo(VideoSearchIndexDetails video, boolean isWebReq, String orgId) {
        annotateVideoURLInfo(video, isWebReq, null, orgId);
    }

    protected void annotateVideoURLInfo(VideoSearchIndexDetails video, boolean isWebReq,
            Map<String, String> sessionParams, String orgId) {
        annotateLinkInfo(video);
        if (video.linkType == LinkType.UPLOADED) {
            String[] originalExtension = (video.originalFileName).split("\\.");
            String extension = originalExtension[originalExtension.length - 1];
            LOGGER.debug("backup url extension is " + extension);
            video.url = (video.converted) ? ImageDisplayURLUtil.getEntityVideoSecureURL(
                    EntityType.VIDEO, video.uuid, sessionParams, isWebReq) : ImageDisplayURLUtil
                    .getEntityVideoSecureURL(EntityType.VIDEO, video.uuid, extension,
                            FileCategory.ORIGINAL, sessionParams, isWebReq);
            video.backupVideoUrl = ImageDisplayURLUtil.getEntityVideoSecureURL(EntityType.VIDEO,
                    video.uuid, extension, FileCategory.ORIGINAL, sessionParams, isWebReq);
            Organization org = OrganizationDAO.INSTANCE.getById(orgId);
            if(org.disableDownload){
                video.s3url = (video.converted) ? ImageDisplayURLUtil.getEntityVideoS3URL(
                        EntityType.VIDEO, video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                        FileCategory.CONVERTED) : ImageDisplayURLUtil.getEntityVideoS3URL(
                        EntityType.VIDEO, video.uuid, extension, FileCategory.ORIGINAL);
                video.s3HDurl = ImageDisplayURLUtil.getEntityVideoS3URL(EntityType.VIDEO, video.uuid,
                        extension, FileCategory.ORIGINAL);
            }else{
                video.s3url = video.url;
                video.s3HDurl = video.backupVideoUrl;
            }
            video.disableDownload = org.disableDownload;
        }

        if (StringUtils.isNotEmpty(video.thumbnail)) {
            video.poster = ImageDisplayURLUtil.getEntityPoster(EntityType.VIDEO,
                    video.thumbnail);
            video.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.VIDEO,
                    video.thumbnail);
        } else {
            video.thumbnail = StringUtils.EMPTY;
            video.poster    = StringUtils.EMPTY;
        }

    }

    private void annotateVideoURLInfo(List<? extends VideoSearchIndexDetails> videoList,
            boolean isWebReq, String orgId) {
        for (VideoSearchIndexDetails video : videoList) {
            annotateVideoURLInfo(video, isWebReq, orgId);
        }
    }

    public ListResponse<GetVideoRes> getSimilarVideos(GetSimilarEntities request) {

        ListResponse<GetVideoRes> results = getSimilarEntityInfos(request, GetVideoRes.class, null);
        annotateExtraInfo(request.userId, request.orgId, EntityType.VIDEO, results.list);
        annotateVideoURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        Video content = VideoDAO.INSTANCE.getById(request.entity.id);

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(Video.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.description = request.description;
            updateList.add(Video.DESCRIPTION);

        }

        if (request.updateList.contains(EditContentReq.BOARD_IDS)) {
            content.boardIds = request.boardIds;
            updateList.add(Video.BOARD_IDS);

        }

        VideoDAO.INSTANCE.updateModel(content, updateList);
        generateEventAysc(request.userId, content, EventActionType.UPDATE, EventType.INDEX_VIDEO,
                UserActionType.UPDATED, false);

        return true;
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<Video> videos = new ArrayList<Video>();

        if (StringUtils.isNotEmpty(id)) {
            Video video = VideoDAO.INSTANCE.getById(id);

            if (video == null) {
                return false;
            }
            videos.add(video);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Video) {
                    videos.add((Video) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.VIDEO);
        for (Video video : videos) {
            if( video.size.isFinalized() && !recalculate){
                continue;
            }

            video.size.reset();

            long thumbnailSize = defs.size(video.thumbnail, EntityType.VIDEO,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.SMALL);

            if (video.linkType == LinkType.UPLOADED) {
                long originalSize = defs.size(video.uuid, EntityType.VIDEO,
                        FileUtils.getExtensionWithoutDOT(video.originalFileName), MediaType.VIDEO,
                        FileCategory.ORIGINAL, ImageSize.ORIGINAL);
                long convertedSize = defs.size(video.uuid, EntityType.VIDEO,
                        FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                        FileCategory.CONVERTED, ImageSize.MEDIUM);

                long encrypted = defs.size(video.uuid, EntityType.VIDEO,
                        FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                        FileCategory.ENCRYPTED, ImageSize.MEDIUM);
                video.size.addOriginal(originalSize);
                video.size.addConverted(convertedSize);
                video.size.addEncrypted(encrypted);
            }
            video.size.addThumbnail(thumbnailSize);

            VideoDAO.INSTANCE.updateModel(video, Arrays.asList(Document.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId) throws VedantuException, EntityFileStorageException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        Video video = VideoDAO.INSTANCE.getById(entityId);
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.VIDEO);

        DownloadableFileInfo thumbInfo = new DownloadableFileInfo();
        thumbInfo.entityId = entityId;
        thumbInfo.entityType = entityType;
        thumbInfo.name = AbstractEntityFileStorage.computeFileId(video.thumbnail, EntityType.VIDEO,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                ImageSize.SMALL);
        FileData data =defs.getSecuredURL(video.thumbnail, EntityType.VIDEO,
                FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                ImageSize.SMALL);
        thumbInfo.downloadUrl = data.getSecuredURL();
        thumbInfo.size = data.getContentLength();
        thumbInfo.mediaType= MediaType.IMAGE;
        fileInfos.add(thumbInfo);

        if (video.linkType == LinkType.UPLOADED) {
            DownloadableFileInfo contentInfo = new DownloadableFileInfo();
            contentInfo.entityId = entityId;
            contentInfo.entityType = entityType;
            contentInfo.name = AbstractEntityFileStorage.computeFileId(video.uuid, EntityType.VIDEO,
                    FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO, FileCategory.ENCRYPTED,
                    ImageSize.MEDIUM);
            data = defs.getSecuredURL(video.uuid, EntityType.VIDEO,
                    FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO, FileCategory.ENCRYPTED,
                    ImageSize.MEDIUM);
            contentInfo.size = data.getContentLength();
            contentInfo.downloadUrl=data.getSecuredURL();
            contentInfo.mediaType= MediaType.VIDEO;
            fileInfos.add(contentInfo);
        }

        return fileInfos;
    }

    public GetPlaylistVideosRes getPlaylistVideos(GetPlaylistVideosReq request) {
        GetPlaylistVideosRes response = new GetPlaylistVideosRes();
        Module module = ModuleDAO.INSTANCE.getById(request.moduleId);
        SrcEntity entity = new SrcEntity(EntityType.VIDEO, request.id);
        ModuleEntry entry = new ModuleEntry();
        entry.entity = entity;
        int index = module.children.indexOf(entry);
        for(int i = index+1; i < module.children.size();i++){
            if(response.videoIds.size() == 4){
                break;
            }
            if(module.children.get(i).name == null && module.children.get(i).entity.type == EntityType.VIDEO){
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
                    getVideoRes = getVideo(getVideoReq);
                } catch (VedantuException e) {
                    LOGGER.error("Problem in getting video response");
                    e.printStackTrace();
                }
                response.videoIds.add(getVideoRes);
            }
        }
        return response;
    }
}
