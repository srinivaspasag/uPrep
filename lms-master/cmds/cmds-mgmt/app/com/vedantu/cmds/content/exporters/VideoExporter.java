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
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.VideoEntityFileStorage;
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
import com.vedantu.content.models.Video;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.mongo.VedantuRecordState;

public class VideoExporter extends AbstractContentExporter {

    public static VideoExporter  INSTANCE = new VideoExporter();
    private static final ALogger LOGGER   = Logger.of(VideoExporter.class);

    @Override
    public boolean export(ExportRecordManager manager, EntityExportRecord entityRecord,
            SrcEntity target) throws ExportException, OperationAbortedException {

        ExportRecord record = getExportRecord(manager.exportId);
        FileData data = null;
        try {

            Video video = (Video) verifyPublishing(entityRecord.content);

            SrcEntity videoEntity = new SrcEntity(EntityType.VIDEO, video._getStringId());
            GetContentLinkRes resource;
            try {
                resource = ContentManager.getContentLink(record.contentSrc.id, record.userId,
                        videoEntity, target, UserActionType.ADDED, VedantuRecordState.ACTIVE);
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
                throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, e);
            }

            VideoEntityFileStorage storage = new VideoEntityFileStorage();

            String thumbnailFileName = AbstractEntityFileStorage.computeFileId(video.thumbnail,
                    EntityType.VIDEO, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                    FileCategory.CONVERTED, ImageSize.SMALL);

            data = storage.getData(EntityType.VIDEO.name(), MediaType.IMAGE.getAcronym(),
                    thumbnailFileName);
            entityRecord.exportedSize += data.getContentLength();
            manager.writeFile(ExportRecordManager.THUMBS, thumbnailFileName, data);
            if (video.linkType == LinkType.UPLOADED) {

                String videoFileName = null;
                if (manager.encryptionLevel != EncryptionLevel.NA) {
                    resource.encLevel = manager.securityManager.getEffectivEncLevel(
                            manager.encryptionLevel, resource.id, record.contentSrc.id);
                    resource.passphrase = manager.securityManager.getPassphrase(
                            manager.encryptionLevel, resource.id, record.targetUserId,
                            record.contentSrc.id);

                    videoFileName = AbstractEntityFileStorage.computeFileId(video.uuid,
                            EntityType.VIDEO, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                            MediaType.VIDEO, FileCategory.ENCRYPTED, null);

                } else {

                    videoFileName = AbstractEntityFileStorage.computeFileId(video.uuid,
                            EntityType.VIDEO, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                            MediaType.VIDEO, FileCategory.CONVERTED, null);
                }

                data = storage.getData(EntityType.VIDEO.name(), MediaType.VIDEO.getAcronym(),
                        videoFileName);

                manager.writeFile(ExportRecordManager.VIDEOS, videoFileName, data);
                entityRecord.exportedSize += data.getContentLength();
            }
            manager.metadataFileWriter.writeContent(resource);

        } catch (EntityFileStorageException e) {
            LOGGER.error("Storage exception occured", e);
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
