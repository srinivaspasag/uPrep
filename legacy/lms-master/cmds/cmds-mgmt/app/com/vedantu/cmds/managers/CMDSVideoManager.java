package com.vedantu.cmds.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.models.event.details.VideoTranscodingDetails;
import com.vedantu.cmds.models.event.search.details.CMDSVideoSearchIndexDetails;
import com.vedantu.cmds.pojos.requests.videos.ConfirmVideoUploadReq;
import com.vedantu.cmds.pojos.requests.videos.GetCMDSVideoReq;
import com.vedantu.cmds.pojos.responses.videos.GetCMDSVideoRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.media.AudioPresets;
import com.vedantu.commons.entity.media.VideoPresets;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSVideoStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.exception.FileHandlerFactory;
import com.vedantu.commons.fs.exception.FileHandlerFactory.HandlerType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.fs.managers.DownloadImageManager;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.managers.VideoManager;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;

public class CMDSVideoManager extends AbstractCMDSContentManager {

    public static final String     FILE_NAME = "fileName";

    public static CMDSVideoManager INSTANCE  = new CMDSVideoManager();

    private final static ALogger   LOGGER    = Logger.of(CMDSVideoManager.class);

    public CMDSVideoManager() {

    }

    public GetCMDSVideoRes confirmVideo(ConfirmVideoUploadReq request) throws VedantuException {

        CMDSVideo cmdsVideo = null;
        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        if (StringUtils.isEmpty(request.userId)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        if (request.type == null
                || !(request.type == LinkType.UPLOADED || request.type == LinkType.ADDED)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TYPE);
        }

        if (request.type == LinkType.UPLOADED) {

            cmdsVideo = CMDSVideoDAO.INSTANCE
                    .getById(request.videoId, VedantuRecordState.TEMPORARY);

            if (StringUtils.isEmpty(request.originalFileName) || StringUtils.isEmpty(request.uuid)) {
                throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
            }

            if (cmdsVideo == null || !cmdsVideo.uuid.equals(request.uuid)
                    || !cmdsVideo.originalFileName.equals(request.originalFileName)) {
                throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND);
            }

            cmdsVideo.stored = true;
            cmdsVideo.extension = FileUtils.getExtensionWithoutDOT(request.originalFileName);
            cmdsVideo.usage = request.usage;
            CMDSVideoStorage ds = new CMDSVideoStorage();

            String computedFileId = AbstractEntityFileStorage.computeFileId(cmdsVideo.uuid,
                    EntityType.CMDSVIDEO,
                    FileUtils.getExtensionWithoutDOT(cmdsVideo.originalFileName), MediaType.VIDEO,
                    FileCategory.ORIGINAL, null);

            boolean fileFoundInStorage = false;
            try {
                fileFoundInStorage = FileSystemFactory.INSTANCE.getFS().exists(ds.getStorageId(),
                        request.uploadedFileName);
            } catch (FileStoreException e) {
                throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
            }

            if (!computedFileId.equals(request.uploadedFileName) || !fileFoundInStorage) {
                throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
            }

            if(cmdsVideo.usage.equalsIgnoreCase("web")){
                cmdsVideo.converted = true;
            }

            // generate thumnbnail
        } else if (request.type == LinkType.ADDED) {
            cmdsVideo = new CMDSVideo();
            if (StringUtils.isNotEmpty(request.thumbnail)) {

                String uuid = UUID.randomUUID().toString();
                String imageName = uuid + FileUtils.JPG_EXTENTION;
                String imageTempOutputPath = getImageLocalTempPath(imageName);
                LOGGER.info("downloading image to : " + imageTempOutputPath);

                File thumbnailFile = DownloadImageManager.downloadImage(request.thumbnail,
                        imageTempOutputPath, FileUtils.JPG_EXTENTION_WITHOUT_DOT);

                IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                        .get(EntityType.CMDSVIDEO);
                Map<String, String> tags = new HashMap<String, String>();
                tags.put(ConstantsGlobal.ORG_ID, request.orgId);
                tags.put(ConstantsGlobal.ENTITY_TYPE, EntityType.CMDSVIDEO.name());

                try {

                    storage.storeImage(uuid, thumbnailFile, FileCategory.CONVERTED,
                            ImageSize.SMALL, tags);

                } catch (EntityFileStorageException e) {
                    throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
                }
                cmdsVideo.thumbnail = uuid;
                cmdsVideo.uuid = uuid;
                cmdsVideo.linkInfo = request.linkInfo;
                cmdsVideo.converted = true;

            }

            cmdsVideo.url = request.url;
        }
        CMDSFolder folder = null;
        if (StringUtils.isEmpty(request.folderId)) {
            folder = CMDSFolderDAO.INSTANCE.getRootFolder(request.orgId);
        } else {
            folder = CMDSFolderDAO.INSTANCE.getById(request.folderId);
        }

        if (folder == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
        }
        cmdsVideo.linkType = request.type;
        cmdsVideo.difficulty = request.difficulty;
        cmdsVideo.duration = request.duration;
        cmdsVideo.description = request.description;
        cmdsVideo.userId = request.userId;
        cmdsVideo.name = request.name;

        // setting tags
        cmdsVideo.tags = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.tags)) {
            cmdsVideo.tags.addAll(request.tags);
        }
        // setting boardIds
        cmdsVideo.boardIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.brdIds)) {
            cmdsVideo.boardIds.addAll(request.brdIds);
        }

        // setting targetIds
        cmdsVideo.targetIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(request.targetIds)) {
            cmdsVideo.targetIds.addAll(request.targetIds);
        }
        if (StringUtils.isNotEmpty(request.orgId)) {
            cmdsVideo.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
        }
        cmdsVideo.recordState = VedantuRecordState.ACTIVE;
        cmdsVideo.completed = CMDSVideoDAO.INSTANCE.isReadyToPublished(cmdsVideo);

        CMDSVideoDAO.INSTANCE.addVideo(cmdsVideo);
        generateEventAysc(request.userId, cmdsVideo, EventActionType.ADD,
                EventType.INDEX_CMDS_VIDEO, UserActionType.ADDED, false);
        SrcEntity cmdsVideoEntity = new SrcEntity(EntityType.CMDSVIDEO, cmdsVideo._getStringId());
        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(cmdsVideoEntity,
                EventActionType.ADD, cmdsVideo);
        CMDSResourcesManager.addToFolder(request.orgId, request.userId, cmdsVideoEntity,
                request.folderId, CmdsContentLinkType.ADDED, parentESId);

        GetCMDSVideoRes response = new GetCMDSVideoRes();
        response.fromMongoModel(cmdsVideo);
        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSVIDEO, response);
        annotateVideoURLInfo(response, request.isWebReq());

        if (cmdsVideo.linkType == LinkType.UPLOADED) {
            if (response.operationJobIdMap == null) {
                response.operationJobIdMap = new HashMap<OperationType, String>();
            }
            startProcessingUploadVideo(cmdsVideo, response.operationJobIdMap);
        }

        return response;
    }

    public void startReprocessingVideo(CMDSVideo cmdsVideo,int bitrate) {

        Map<OperationType, String> operationJobIdMap = new HashMap<OperationType, String>();

        EntityOperationStatus status = new EntityOperationStatus();

        VideoTranscodingDetails details = new VideoTranscodingDetails();

        details.audioPreset = new AudioPresets();
        details.videoPreset = new VideoPresets();
        details.videoPreset.fileExt = FileUtils.WEBM_EXTENTION_WITHOUT_DOT;
        if(bitrate!=0){

        details.videoPreset.bitrate = bitrate;
        }
        details.videoId = cmdsVideo._getStringId();

        details.generateThumbnail = true;
        details.generateFileSize = true;
        details.generateDuration = true;
        details.generateNewVideo = !FileUtils.getExtensionWithoutDOT(cmdsVideo.originalFileName)
                .equalsIgnoreCase(FileUtils.WEBM_EXTENTION_WITHOUT_DOT);
        if (StringUtils.isNotEmpty(cmdsVideo.passphrase)) {
            details.encryptIfNeeded = true;

        }
        status.numOfSteps += details.generateNewVideo ? 1 : 0;
        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;
        status.numOfSteps += details.generateThumbnail ? 1 : 0;
        status.numOfSteps += details.generateFileSize ? 1 : 0;
        status.numOfSteps += details.generateDuration ? 1 : 0;

        if (status.numOfSteps > 0) {
            status.oType = OperationType.VIDEO_CONVERSION;
            status.id = cmdsVideo._getStringId();
            status.type = EntityType.CMDSVIDEO;
            EntityOperationStatusDAO.INSTANCE.save(status);
            details.jobId = status._getStringId();

            generateEventAysc(cmdsVideo.userId, details, EventType.CONVERT_VIDEO);
            operationJobIdMap.put(status.oType, status._getStringId());
        }

    }

    private void startProcessingUploadVideo(CMDSVideo cmdsVideo,
            Map<OperationType, String> operationJobIdMap) {
        LOGGER.info("cmdsVideo : "+cmdsVideo);
        if (operationJobIdMap == null) {
            return;
        }

        EntityOperationStatus status = new EntityOperationStatus();

        VideoTranscodingDetails details = new VideoTranscodingDetails();

        details.audioPreset = new AudioPresets();
        details.videoPreset = new VideoPresets();
        details.videoPreset.fileExt = FileUtils.WEBM_EXTENTION_WITHOUT_DOT;
        if(StringUtils.isNotEmpty(cmdsVideo.usage) && cmdsVideo.usage.equals("sdcard")){
            details.videoPreset.bitrate = -2;
        }
        details.videoId = cmdsVideo._getStringId();

        details.generateThumbnail = true;
        details.generateFileSize = false;
        details.generateDuration = true;
        details.generateNewVideo = !FileUtils.getExtensionWithoutDOT(cmdsVideo.originalFileName)
                .equalsIgnoreCase(FileUtils.WEBM_EXTENTION_WITHOUT_DOT);
        if (StringUtils.isNotEmpty(cmdsVideo.passphrase)) {
            details.encryptIfNeeded = true;

        }
        status.numOfSteps += details.generateNewVideo ? 1 : 0;
        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;
        status.numOfSteps += details.generateThumbnail ? 1 : 0;
        status.numOfSteps += details.generateFileSize ? 1 : 0;
        status.numOfSteps += details.generateDuration ? 1 : 0;

        if (status.numOfSteps > 0) {
            status.oType = OperationType.VIDEO_CONVERSION;
            status.id = cmdsVideo._getStringId();
            status.type = EntityType.CMDSVIDEO;
            EntityOperationStatusDAO.INSTANCE.save(status);
            details.jobId = status._getStringId();

            generateEventAysc(cmdsVideo.userId, details, EventType.CONVERT_VIDEO);
            operationJobIdMap.put(status.oType, status._getStringId());
        }

    }


    private String getImageLocalTempPath(String fileName) {

        LocalFileSystemHandler tempLocHandler = (LocalFileSystemHandler) FileHandlerFactory
                .get(HandlerType.TEMP);
        return tempLocHandler.getDirectory() + LocalFileSystemHandler.PATH_SEPARATOR + fileName;

    }

    public GetCMDSVideoRes getVideo(GetCMDSVideoReq request) throws VedantuException {

        CMDSVideo video = CMDSVideoDAO.INSTANCE.getById(request.id);
        GetCMDSVideoRes getVideoRes = new GetCMDSVideoRes();
        getVideoRes.fromMongoModel(video);

        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSVIDEO, getVideoRes);
        annotateVideoURLInfo(getVideoRes, request.isWebReq(), request.__getSessionParams());
        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getByOType(
                EntityType.CMDSVIDEO, video._getStringId(), OperationType.VIDEO_CONVERSION);
        if (status != null) {
            if (status.numOfSteps != 0 && status.numOfSteps == status.numOfStepsCompleted) {
                EntityOperationStatusDAO.INSTANCE.markDeleted(status);
            } else {

                getVideoRes.operationJobIdMap.put(OperationType.VIDEO_CONVERSION,
                        status._getStringId());
            }
        }
        return getVideoRes;
    }

    private void annotateVideoURLInfo(CMDSVideoSearchIndexDetails video, boolean isWebReq) {

        annotateVideoURLInfo(video, isWebReq, null);
    }

    private void annotateVideoURLInfo(CMDSVideoSearchIndexDetails video, boolean isWebReq,
            Map<String, String> sessionParams) {

        annotateLinkInfo(video);

        if (video.linkType == LinkType.UPLOADED) {
            String[] originalExtension = (video.originalFileName).split("\\.");
            String extension = originalExtension[originalExtension.length - 1];
            video.url = (video.converted) ? ImageDisplayURLUtil.getEntityVideoSecureURL(
                    EntityType.CMDSVIDEO, video.uuid, sessionParams, isWebReq)
                    : ImageDisplayURLUtil.getEntityVideoSecureURL(EntityType.CMDSVIDEO, video.uuid,
                            video.extension, FileCategory.ORIGINAL, sessionParams, isWebReq);
            video.backupVideoUrl = ImageDisplayURLUtil.getEntityVideoSecureURL(EntityType.VIDEO, video.uuid, extension,
                            FileCategory.ORIGINAL, sessionParams, isWebReq);
            if(sessionParams != null){
                if(sessionParams.get("orgId") != null){
                    Organization org = OrganizationDAO.INSTANCE.getById(sessionParams.get("orgId"));
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
                }else{
                    video.s3url = video.url;
                    video.s3HDurl = video.backupVideoUrl;
                }
            }else{
                video.s3url = video.url;
                video.s3HDurl = video.backupVideoUrl;
            }
        }

        if (StringUtils.isNotEmpty(video.thumbnail)) {
            video.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.VIDEO,
                    video.thumbnail);
        } else {
            video.thumbnail = StringUtils.EMPTY;
        }

    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        CMDSVideo content = CMDSVideoDAO.INSTANCE.getById(request.entity.id);

        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(CMDSVideo.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.description = request.description;
            updateList.add(CMDSVideo.DESCRIPTION);

        }

        if (request.updateList.contains(EditContentReq.BOARD_IDS)) {
            request.boardIds = new HashSet<String>();
            request.boardIds.addAll(request.cmdsBoardIds);
            content.boardIds = request.boardIds;
            updateList.add(CMDSVideo.BOARD_IDS);

        }

        boolean updated = true;
        if (content.globalVideoId != null) {
            request.entity = new SrcEntity(EntityType.VIDEO, content.globalVideoId);
            updated = VideoManager.INSTANCE.update(request);
        }

        if (updated) {
            CMDSVideoDAO.INSTANCE.updateModel(content, updateList);
            CMDSModuleDAO.INSTANCE.updateModuleStatus(request.entity);
            // TODO SrcEntity entity = new SrcEntity(); check with Shivank
            // entity.id = content.id;
            // entity.type = EntityType.CMDSVIDEO;
            LibraryContentLinksDAO.INSTANCE.updateLastUpdated(request.entity);
            generateEventAysc(request.userId, content, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_VIDEO, UserActionType.UPDATED, false);

            addAsCMDSResource(request.entity, EventActionType.UPDATE, content);
        }

        return updated;
    }

    @Override
    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSVideo> videos = new ArrayList<CMDSVideo>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSVideo question = CMDSVideoDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            videos.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSVideo) {
                    videos.add((CMDSVideo) content);
                }
            }
        }

        // calculate question image size;
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.CMDSVIDEO);
        for (CMDSVideo video : videos) {
            if( video.size.isFinalized() && !recalculate){
                continue;
            }
            video.size.reset();
            LOGGER.debug("Logging for linktype" + video.linkType);
            long thumbnailSize = defs.size(video.thumbnail, EntityType.CMDSVIDEO,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.SMALL);

            if (video.linkType == LinkType.UPLOADED) {
                LOGGER.debug("Calculating content sizes" + video.linkType);
                long convertedSize = defs.size(video.uuid, EntityType.CMDSVIDEO,
                        FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                        FileCategory.CONVERTED, ImageSize.MEDIUM);

                long encrypted = defs.size(video.uuid, EntityType.CMDSVIDEO,
                        FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                        FileCategory.ENCRYPTED, ImageSize.MEDIUM);
                long originalSize = defs.size(video.uuid, EntityType.CMDSVIDEO,
                        FileUtils.getExtensionWithoutDOT(video.originalFileName), MediaType.VIDEO,
                        FileCategory.ORIGINAL, ImageSize.ORIGINAL);
                video.size.addOriginal(originalSize);
                video.size.addConverted(convertedSize);
                video.size.addEncrypted(encrypted);
            }

            video.size.addThumbnail(thumbnailSize);

            CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList(CMDSVideo.SIZE));
            if (video.globalVideoId != null) {
                VideoManager.INSTANCE.calculate(video.globalVideoId,true);
            }
        }
        return true;

    }
}
