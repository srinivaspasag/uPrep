package com.vedantu.cmds.daos;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSDocumentDAO extends CmdsContentDAO<CMDSDocument, ObjectId> implements
        ICMDSResource, IDownloadable, IPublishable {

    private static final ALogger LOGGER = Logger.of(CMDSDocumentDAO.class);

    private CMDSDocumentDAO() {

        super(CMDSDocument.class);
    }

    public static final CMDSDocumentDAO INSTANCE = new CMDSDocumentDAO();

    public void addDocument(CMDSDocument document) {

        save(document);
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSDocument cmdsDocument = (CMDSDocument) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSDOCUMENT, model._getStringId());
        details.queryContext = cmdsDocument.description;

        return details;

    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSDocument cmdsDocument = CMDSDocumentDAO.INSTANCE.getById(id);
        if (cmdsDocument != null) {
            return DocumentDAO.INSTANCE.getDocument(cmdsDocument.globalDocId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(id);
        if (StringUtils.isNotEmpty(document.globalDocId)) {
            return new SrcEntity(EntityType.DOCUMENT, document.globalDocId);
        }
        return null;
    }

    @Override
    public boolean isPublished(String id) {

        CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(id);
        return document.published;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(id);
        return (document.completed && document.converted);
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSDocument) {

            CMDSDocument document = (CMDSDocument) cmdsModel;

            boolean canBePublished = true;

            if (canBePublished && (document.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                LOGGER.debug(" Video is not in active state id:" + document._getStringId());
            }

            if (canBePublished
                    && (document.linkType == LinkType.ADDED && StringUtils.isEmpty(document.url))) {
                canBePublished &= false;
                LOGGER.debug(" Video is linked but no url:" + document._getStringId());
            }

            if (canBePublished
                    && (document.linkType == LinkType.UPLOADED && StringUtils
                            .isEmpty(document.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" Video is added but no uuid state id:" + document._getStringId());
            }

            if (canBePublished
                    && (document.linkType == LinkType.UPLOADED && StringUtils
                            .isEmpty(document.uuid))) {
                canBePublished &= false;
                LOGGER.debug(" Video is uploaded but no uuid associated with it  id:"
                        + document._getStringId());
            }

            if (canBePublished && (CollectionUtils.isEmpty(document.boardIds))) {
                canBePublished &= false;
                LOGGER.debug(" No boards provided for Video id:" + document._getStringId());
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

        CMDSDocument currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof CMDSDocument) {
                currentRecord = (CMDSDocument) record;
            }
        }

        return FileUtils.getFileName(currentRecord.originalFileName);
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSDocument) {
            CMDSDocument document = (CMDSDocument) cmdsModel;
            return document.published;
        }

        return false;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSDocument)) {
            return false;
        }
        CMDSDocument document = (CMDSDocument) model;

        if (document.published == true || document.globalDocId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(document);
        updateModel(document, Arrays.asList(ConstantsGlobal.RECORD_STATE));

        return true;

    }


}
