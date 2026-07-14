package com.vedantu.cmds.daos;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSFileDAO extends CmdsContentDAO<CMDSFile, ObjectId> implements ICMDSResource,
        IDownloadable, IPublishable {

    private static final ALogger LOGGER = Logger.of(CMDSFileDAO.class);

    private CMDSFileDAO() {

        super(CMDSFile.class);
    }

    public static final CMDSFileDAO INSTANCE = new CMDSFileDAO();

    public void addFile(CMDSFile file) {

        save(file);
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSFile cmdsFile = (CMDSFile) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSFILE, model._getStringId());
        details.queryContext = cmdsFile.description;

        return details;

    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSFile cmdsFile = CMDSFileDAO.INSTANCE.getById(id);
        if (cmdsFile != null) {
            return FileDAO.INSTANCE.getFile(cmdsFile.globalFileId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSFile file = CMDSFileDAO.INSTANCE.getById(id);
        if (StringUtils.isNotEmpty(file.globalFileId)) {
            return new SrcEntity(EntityType.FILE, file.globalFileId);
        }
        return null;
    }

    @Override
    public boolean isPublished(String id) {

        CMDSFile file = CMDSFileDAO.INSTANCE.getById(id);
        if (file == null) {
            return false;
        }

        return file.published;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSFile file = CMDSFileDAO.INSTANCE.getById(id);
        if (file == null) {
            return false;
        }

        return (file.completed);// && file.converted);
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSFile) {
            CMDSFile file = (CMDSFile) cmdsModel;
            boolean canBePublished = true;

            if (canBePublished && (file.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                LOGGER.debug(" File is not in active state id:" + file._getStringId());
            }

            if (canBePublished
                    && (file.linkType == LinkType.ADDED && StringUtils.isEmpty(file.url))) {
                canBePublished &= false;
                LOGGER.debug(" File is added but no url:" + file._getStringId());
            }

            if (canBePublished
                    && (file.linkType == LinkType.ADDED && StringUtils.isEmpty(file.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" File is added but no uuid state id:" + file._getStringId());
            }

            if (canBePublished
                    && (file.linkType == LinkType.UPLOADED && StringUtils.isEmpty(file.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" File is uploaded but no uuid associated with it  id:"
                        + file._getStringId());
            }

            if (canBePublished && (CollectionUtils.isEmpty(file.boardIds))) {
                canBePublished &= false;
                LOGGER.debug(" No boards provided for Video id:" + file._getStringId());
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

        CMDSFile currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof CMDSFile) {
                currentRecord = (CMDSFile) record;
            }
        }

        return FileUtils.getFileName(currentRecord.originalFileName);
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSFile) {
            CMDSFile document = (CMDSFile) cmdsModel;
            return document.published;
        }

        return false;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSFile)) {
            return false;
        }

        CMDSFile file = (CMDSFile) model;

        if (file.published == true || file.globalFileId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(file);
        updateModel(file, Arrays.asList(ConstantsGlobal.RECORD_STATE));
        return true;

    }

}
