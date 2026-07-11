package com.vedantu.content.utils;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.commons.utils.FileMaskProcessor;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.models.AbstractFileModel;

public class FileEncryptor {

    public static final ALogger LOGGER = Logger.of(FileEncryptor.class);

    /**
     * returns name of stored encrypted file
     * 
     * @param storage
     * @param contentModel
     * @param inputFile
     * @param mediaType
     * @return
     * @throws Exception
     * @throws VedantuException
     */
    public static String encrypt(AbstractEntityFileStorage storage, AbstractFileModel contentModel,
            File inputFile, MediaType mediaType) throws Exception, VedantuException {

        String fileName = AbstractEntityFileStorage.computeFileId(contentModel.uuid,
                storage.getEntityType(), FileUtils.getExtensionWithoutDOT(inputFile.getName()),
                mediaType, FileCategory.ENCRYPTED, null);
        String encDestination = FileSystemFactory.INSTANCE.getTempFS().getFilePath(
                storage.getStorageId(), fileName);

        final File encryptedFile = new File(encDestination);
        LOGGER.debug("Encrypted File path will be : " + encryptedFile.getAbsolutePath());
        if (StringUtils.isEmpty(contentModel.passphrase)) {
            LOGGER.error("no passphrase" + contentModel.getClass() + " " + contentModel.id);
            return null;
        }
        try {
            FileMaskProcessor xorVideo = new FileMaskProcessor(contentModel.passphrase,
                    EncryptionUtils.MAXIMUM_PASSPHRASE_SIZE);

            final int encReadBufferSize = EncryptionUtils.MAXIMUM_PASSPHRASE_SIZE;
            LOGGER.debug("starting encryption...");

            boolean isEncrypted = xorVideo.process(inputFile, encReadBufferSize, encryptedFile);

            // TODO check for this one later
            if (!isEncrypted && encryptedFile.length() != inputFile.length()) {
                 throw new VedantuException(VedantuErrorCode.ENCRYPTION_FAILED);
            }
            LOGGER.debug("starting file upload!");
            StorageResult storageResult = storage.store(contentModel.uuid, encryptedFile,
                    mediaType, FileCategory.ENCRYPTED, null);
            if (!storageResult.isStored) {
                throw new VedantuException(VedantuErrorCode.ENCRYPTION_FAILED);
            }

            return storageResult.fileId;
        } catch (EntityFileStorageException e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.ENCRYPTION_FAILED);
        } finally {
            if (encryptedFile.exists()) {
                FileUtils.deleteFile(encryptedFile.getName(), encryptedFile);
            }
        }
    }
}
