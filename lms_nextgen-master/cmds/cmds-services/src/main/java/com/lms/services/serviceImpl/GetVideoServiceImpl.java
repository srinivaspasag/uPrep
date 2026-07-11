package com.lms.services.serviceImpl;

import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.component.CmdsVideoManager;
import com.lms.components.VideoManagerComponent;
import com.lms.enums.SrcType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSVideo;
import com.lms.models.Organization;
import com.lms.models.event.search.details.CMDSVideoSearchIndexDetails;
import com.lms.pojos.requests.GetCMDSVideoReq;
import com.lms.pojos.responce.EditContentRes;
import com.lms.pojos.responce.GetCMDSVideoRes;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import com.lms.repo.CMDSVideoRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.services.CmdsVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class GetVideoServiceImpl extends AbstractContentManager implements CmdsVideoService {

    @Autowired
    private CMDSVideoRepo cmdsVideoRepo;
    @Autowired
    private EntityOperationStatusRepo entityOperationStatusRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    //    @Autowired
//    private AbstractContentManager abstractContentManager;
    @Autowired
    private VideoManagerComponent videoManagerComponent;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private CmdsVideoManager cmdsVideoManager;

    @Override
    public VedantuResponse getVideo(GetCMDSVideoReq request) {

        CMDSVideo video = cmdsVideoRepo.findById(request.id).get();
        GetCMDSVideoRes getVideoRes = new GetCMDSVideoRes();
        getVideoRes.fromMongoModel(video);

        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSVIDEO, getVideoRes);
        annotateVideoURLInfo(getVideoRes, request.isWebReq(), request.__getSessionParams());
        EntityOperationStatus status = getByOType(
                EntityType.CMDSVIDEO, video._getStringId(), OperationType.VIDEO_CONVERSION);
        if (status != null) {
            if (status.numOfSteps != 0 && status.numOfSteps == status.numOfStepsCompleted) {
                markDeleted(status);
            } else {

                getVideoRes.operationJobIdMap.put(OperationType.VIDEO_CONVERSION,
                        status._getStringId());
            }
        }
        return new VedantuResponse(getVideoRes);
    }

    @Override
    public VedantuResponse reporocess(String id) {
        EditContentRes response = new EditContentRes();
        CMDSVideo cmdsVideo = cmdsVideoRepo.findById(id).get();
        int bitrate = -2;
        cmdsVideoManager.startReprocessingVideo(cmdsVideo, bitrate);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse convertAgain(String id) {
        EditContentRes response = new EditContentRes();
        CMDSVideo cmdsVideo = cmdsVideoRepo.findById(id).get();
        int bitrate = 0;
        cmdsVideoManager.startReprocessingVideo(cmdsVideo, bitrate);
        return new VedantuResponse(response);
    }

    public void annotateVideoURLInfo(CMDSVideoSearchIndexDetails video, boolean isWebReq,
                                     Map<String, String> sessionParams) {

        annotateLinkInfo(video);

        if (video.linkType == SrcType.LinkType.UPLOADED) {
            String[] originalExtension = (video.originalFileName).split("\\.");
            String extension = originalExtension[originalExtension.length - 1];
            video.url = (video.converted) ? ImageDisplayURLUtil.getEntityVideoSecureURL(
                    EntityType.CMDSVIDEO, video.uuid, sessionParams, isWebReq)
                    : ImageDisplayURLUtil.getEntityVideoSecureURL(EntityType.CMDSVIDEO, video.uuid,
                    video.extension, FileCategory.ORIGINAL, sessionParams, isWebReq);
            video.backupVideoUrl = ImageDisplayURLUtil.getEntityVideoSecureURL(EntityType.VIDEO, video.uuid, extension,
                    FileCategory.ORIGINAL, sessionParams, isWebReq);
            if (sessionParams != null) {
                if (sessionParams.get("orgId") != null) {
                    Organization org = organizationRepo.findById(sessionParams.get("orgId")).get();
                    if (org.disableDownload) {
                        video.s3url = (video.converted) ? ImageDisplayURLUtil.getEntityVideoS3URL(
                                EntityType.VIDEO, video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                                FileCategory.CONVERTED) : ImageDisplayURLUtil.getEntityVideoS3URL(
                                EntityType.VIDEO, video.uuid, extension, FileCategory.ORIGINAL);
                        video.s3HDurl = ImageDisplayURLUtil.getEntityVideoS3URL(EntityType.VIDEO, video.uuid,
                                extension, FileCategory.ORIGINAL);
                    } else {
                        video.s3url = video.url;
                        video.s3HDurl = video.backupVideoUrl;
                    }
                } else {
                    video.s3url = video.url;
                    video.s3HDurl = video.backupVideoUrl;
                }
            } else {
                video.s3url = video.url;
                video.s3HDurl = video.backupVideoUrl;
            }
        }

        if (!StringUtils.isEmpty(video.thumbnail)) {
            video.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.VIDEO,
                    video.thumbnail);
        } else {
            video.thumbnail = "";
        }

    }

    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    public void markDeleted(EntityOperationStatus entity) {

        entity.recordState = VedantuRecordState.DELETED;
        // updateState(entity, VedantuRecordState.DELETED);
    }

    public EntityOperationStatus getByOType(EntityType type, String id, OperationType oType) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("id").is(id);
        criteria.and("type").is(type);
        criteria.and("oType").is(oType);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, EntityOperationStatus.class);
    }
}
