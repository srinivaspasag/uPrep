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
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.FileStorage;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.File;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.mongo.VedantuRecordState;

public class FileExporter extends AbstractContentExporter {

    public static FileExporter   INSTANCE = new FileExporter();
    private static final ALogger LOGGER   = Logger.of(FileExporter.class);

    @Override
    public boolean export(ExportRecordManager manager, EntityExportRecord entityRecord,
            SrcEntity target) throws ExportException, OperationAbortedException {

        ExportRecord record = getExportRecord(manager.exportId);
        FileData data = null;
        try {

            File file = (File) verifyPublishing(entityRecord.content);

            SrcEntity fileEntity = new SrcEntity(EntityType.FILE, file._getStringId());
            GetContentLinkRes resource;
            try {
                resource = ContentManager.getContentLink(record.contentSrc.id, record.userId,
                        fileEntity, target, UserActionType.ADDED, VedantuRecordState.ACTIVE);
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
                throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, "link not found ");
            }


            FileStorage storage = new FileStorage();

            String fileName = null;
            if (manager.encryptionLevel != EncryptionLevel.NA) {
                fileName = AbstractEntityFileStorage.computeFileId(file.uuid, EntityType.FILE,
                        file.extension, MediaType.FILE, FileCategory.ENCRYPTED, null);
                resource.encLevel = manager.securityManager.getEffectivEncLevel(
                        manager.encryptionLevel, resource.id, record.contentSrc.id);
                resource.passphrase = manager.securityManager.getPassphrase(
                        manager.encryptionLevel, resource.id, record.targetUserId,
                        record.contentSrc.id);
            } else {
                fileName = AbstractEntityFileStorage.computeFileId(file.uuid, EntityType.FILE,
                        file.extension, MediaType.FILE, FileCategory.ORIGINAL, null);

            }

            data = storage.getData(EntityType.FILE.name(), MediaType.FILE.getAcronym(), fileName);
            manager.writeFile(ExportRecordManager.FILES, fileName, data);
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
