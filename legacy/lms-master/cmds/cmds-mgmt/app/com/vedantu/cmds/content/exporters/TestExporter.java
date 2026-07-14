package com.vedantu.cmds.content.exporters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.factory.ContentExporterFactory;
import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.mgmt.interfaces.IContentExporter;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.mongo.VedantuRecordState;

public class TestExporter extends AbstractContentExporter {

    public static TestExporter   INSTANCE = new TestExporter();
    private static final ALogger LOGGER   = Logger.of(TestExporter.class);

    @Override
    public boolean export(ExportRecordManager manager, EntityExportRecord entityRecord,
            SrcEntity target) throws OperationAbortedException, ExportException {

        ExportRecord record = getExportRecord(manager.exportId);
        FileData data = null;
        try {

            Test globalTest = (Test) verifyPublishing(entityRecord.content);

            SrcEntity globalAssignmentEntity = new SrcEntity(EntityType.TEST,
                    globalTest._getStringId());
            GetContentLinkRes resource;
            try {
                resource = ContentManager.getContentLink(record.contentSrc.id, record.userId,
                        new SrcEntity(EntityType.TEST, globalTest._getStringId()), target,
                        UserActionType.ADDED, VedantuRecordState.ACTIVE);
            } catch (VedantuException e) {
                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
            }

            if (resource == null) {
                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT,
                        "es index data not found");
            }

            // Test globalTest = TestDAO.INSTANCE.getById(globalTestEntity.id);
            if (CollectionUtils.isNotEmpty(globalTest.__getAllQIds())) {
                for (String questionId : globalTest.__getAllQIds()) {
                    IContentExporter contentExporter = ContentExporterFactory.INSTANCE
                            .get(EntityType.QUESTION);
                    EntityExportRecord questionRecord = new EntityExportRecord(new SrcEntity(
                            EntityType.QUESTION, questionId), globalTest.timeCreated);
                    contentExporter.export(manager, questionRecord, globalAssignmentEntity);
                    record.exportedSize += questionRecord.exportedSize;
                }
            }
            manager.metadataFileWriter.writeContent(resource);

        } finally {
            if (data != null) {
                IOUtils.closeQuietly(data.getIn());
            }
        }
        return true;

    }

}
