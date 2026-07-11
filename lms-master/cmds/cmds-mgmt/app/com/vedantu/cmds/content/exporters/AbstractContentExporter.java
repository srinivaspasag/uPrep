package com.vedantu.cmds.content.exporters;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.ExportRecordDAO;
import com.vedantu.cmds.enums.ExportState;
import com.vedantu.cmds.mgmt.interfaces.IContentExporter;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public abstract class AbstractContentExporter implements IContentExporter {

    private static final ALogger LOGGER = Logger.of(AbstractContentExporter.class);

    // get storage location from export id
    // get metad

    private String getBasePath(String exportRecordId) throws VedantuException {

        String parentDirectory = EntityType.EXPORTRECORD.name().toLowerCase()
                + FileUtils.SEPARATOR_FWDSLASH + exportRecordId;

        return parentDirectory;

    }

    public static ExportRecord getExportRecord(String exportId) throws OperationAbortedException {

        ExportRecord record = ExportRecordDAO.INSTANCE.getById(exportId);
        if (record.state == ExportState.CANCELLED
                || record.recordState == VedantuRecordState.DELETED) {
            throw new OperationAbortedException(VedantuErrorCode.EXPORT_ABORTED);
        }
        return record;
    }

    public static VedantuBaseMongoModel verifyPublishing(
            SrcEntity content) throws ExportException {

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                .get(content.type);

        if (basicDAO instanceof IPublishable) {

            VedantuBaseMongoModel mongoModel = basicDAO.getById(content.id,
                    VedantuRecordState.ACTIVE);
            if (mongoModel == null) {
                LOGGER.debug("content " + content + " not found ");
                throw new ExportException(VedantuErrorCode.CONTENT_NOT_FOUND, content.type.name()
                        + " not published" + content.id);
            }

            IPublishable publishableDAO = ((IPublishable) basicDAO);
            if (!publishableDAO.isPublished(mongoModel)) {
                throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, content.type.name()
                        + " not published" + content.id);
            }
            try {
                VedantuBaseMongoModel globalEntity = ((IPublishable) basicDAO)
                        .getPublishedEntity(content.id);
                if( globalEntity == null ){
                    throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, content.type.name()
                            + " not published" + content.id);
                }
                return globalEntity;
                
            } catch (VedantuException e) {
                throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, content.type.name()
                        + " not published" + content.id);
            }
        }

        throw new ExportException(VedantuErrorCode.CONTENT_NOT_FOUND, content.type.name()
                + " not published" + content.id);

    }

}
