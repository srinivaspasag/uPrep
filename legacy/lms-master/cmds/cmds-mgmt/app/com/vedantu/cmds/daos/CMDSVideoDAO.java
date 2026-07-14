package com.vedantu.cmds.daos;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSVideoDAO extends CmdsContentDAO<CMDSVideo, ObjectId> implements ICMDSResource,
        IDownloadable, IPublishable {

    private static final ALogger LOGGER = Logger.of(CMDSVideoDAO.class);

    private CMDSVideoDAO() {

        super(CMDSVideo.class);
    }

    public static final CMDSVideoDAO INSTANCE = new CMDSVideoDAO();

    public void addVideo(CMDSVideo video) {

        save(video);
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSVideo cmdsVideo = (CMDSVideo) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSVIDEO, model._getStringId());
        details.queryContext = cmdsVideo.description;

        return details;

    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSVideo cmdsVideo = CMDSVideoDAO.INSTANCE.getById(id);
        if (cmdsVideo != null) {
            return VideoDAO.INSTANCE.getVideo(cmdsVideo.globalVideoId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSVideo video = CMDSVideoDAO.INSTANCE.getById(id);
        if (StringUtils.isNotEmpty(video.globalVideoId)) {
            return new SrcEntity(EntityType.VIDEO, video.globalVideoId);
        }
        return null;
    }

    @Override
    public boolean isPublished(String id) {

        CMDSVideo video = CMDSVideoDAO.INSTANCE.getById(id);
        return video.published;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSVideo video = CMDSVideoDAO.INSTANCE.getById(id);
        return (video.completed && video.converted);
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSVideo) {

            CMDSVideo video = (CMDSVideo) cmdsModel;

            boolean canBePublished = true;

            if (canBePublished && (video.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                LOGGER.debug(" Video is not in active state id:" + video._getStringId());
            }

            if (canBePublished
                    && (video.linkType == LinkType.ADDED && StringUtils.isEmpty(video.url))) {
                canBePublished &= false;
                LOGGER.debug(" Video is added but no url:" + video._getStringId());
            }

            if (canBePublished
                    && (video.linkType == LinkType.ADDED && StringUtils.isEmpty(video.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" Video is added but no uuid state id:" + video._getStringId());
            }

            if (canBePublished
                    && (video.linkType == LinkType.UPLOADED && StringUtils.isEmpty(video.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" Video is uploaded but no uuid associated with it  id:"
                        + video._getStringId());
            }

            if (canBePublished && (CollectionUtils.isEmpty(video.boardIds))) {
                canBePublished &= false;
                LOGGER.debug(" No boards provided for Video id:" + video._getStringId());
            }

            return canBePublished;
        }
        return false;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        return null;
    }

    @Override
    public String getDownloadName(String id, VedantuBaseMongoModel record) {

        CMDSVideo currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof CMDSVideo) {
                currentRecord = (CMDSVideo) record;
            }
        }

        return FileUtils.getFileName(currentRecord.originalFileName);

    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSVideo) {
            CMDSVideo video = (CMDSVideo) cmdsModel;
            return video.published;
        }

        return false;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSVideo)) {
            return false;
        }

        CMDSVideo video = (CMDSVideo) model;

        if (video.published == true || video.globalVideoId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(video);
        updateModel(video, Arrays.asList(ConstantsGlobal.RECORD_STATE));

        return true;

    }

}
