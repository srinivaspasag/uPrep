package com.vedantu.cmds.content.exporters;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.DocumentEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.Document;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.mongo.VedantuRecordState;

public class DocumentExporter extends AbstractContentExporter {

    public static DocumentExporter INSTANCE = new DocumentExporter();
    private static final ALogger   LOGGER   = Logger.of(DocumentExporter.class);

    @Override
    public boolean export(ExportRecordManager manager, EntityExportRecord entityRecord,
            SrcEntity target) throws ExportException, OperationAbortedException {

        ExportRecord record = getExportRecord(manager.exportId);
        FileData data = null;
        try {

            Document document = (Document) verifyPublishing(entityRecord.content);

            SrcEntity documentEntity = new SrcEntity(EntityType.DOCUMENT, document._getStringId());

            GetContentLinkRes resource = null;
            try {
                resource = ContentManager.getContentLink(record.contentSrc.id, record.userId,
                        documentEntity, target, UserActionType.ADDED, VedantuRecordState.ACTIVE);
                if (resource == null) {
                    throw new ExportException(VedantuErrorCode.NOT_PUBLISHED,
                            "can not find link for content" + entityRecord.content);
                }

                ContentManager.annotateThumbnailInfo((ContentSearchDetails) resource.content,
                        manager.encryptionLevel != null
                                && manager.encryptionLevel != EncryptionLevel.NA);
            } catch (ExportException ee) {
                throw ee;
            } catch (VedantuException e) {

                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
            }
            DocumentEntityFileStorage storage = new DocumentEntityFileStorage();

            String thumbnailFileName = AbstractEntityFileStorage.computeFileId(document.thumbnail,
                    EntityType.DOCUMENT, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                    FileCategory.CONVERTED, ImageSize.SMALL);

            data = storage.getData(EntityType.DOCUMENT.name(), MediaType.IMAGE.getAcronym(),
                    thumbnailFileName);
            entityRecord.exportedSize += data.getContentLength();
            manager.writeFile(ExportRecordManager.THUMBS, thumbnailFileName, data);
            String documentFileName = null;
            if (manager.encryptionLevel != EncryptionLevel.NA) {
                LOGGER.debug("Encyrption Level" + manager.encryptionLevel);
                documentFileName = AbstractEntityFileStorage.computeFileId(document.uuid,
                        EntityType.DOCUMENT, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.ENCRYPTED, null);
                resource.encLevel = manager.securityManager.getEffectivEncLevel(
                        manager.encryptionLevel, resource.id, record.contentSrc.id);
                resource.passphrase = manager.securityManager.getPassphrase(
                        manager.encryptionLevel, resource.id, record.targetUserId,
                        record.contentSrc.id);
            } else {
                documentFileName = AbstractEntityFileStorage.computeFileId(document.uuid,
                        EntityType.DOCUMENT, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.CONVERTED, null);

            }
            data = storage.getData(EntityType.DOCUMENT.name(), MediaType.DOC.getAcronym(),
                    documentFileName);
            manager.writeFile(ExportRecordManager.DOCUMENTS, documentFileName, data);
            entityRecord.exportedSize += data.getContentLength();
            manager.metadataFileWriter.writeContent(resource);

        } catch (EntityFileStorageException e) {
            LOGGER.error("Exception occured", e);
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
        } catch (ExportException e) {
            throw e;
        } catch (VedantuException e) {
            LOGGER.error("Exception occured", e);
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
        } finally {
            if (data != null) {
                IOUtils.closeQuietly(data.getIn());
            }
        }
        return true;

    }
}
