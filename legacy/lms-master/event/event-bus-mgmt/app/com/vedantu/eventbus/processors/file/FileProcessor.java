package com.vedantu.eventbus.processors.file;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.managers.CMDSDocumentManager;
import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.cmds.models.event.details.FileProcessingDetails;
import com.vedantu.cmds.models.event.details.VideoTranscodingDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSFileStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.FileConversionState;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.utils.FileEncryptor;
import com.vedantu.content.utils.FileModelUtils;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class FileProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(FileProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        // TODO Auto-generated method stub
        // check if file exists locally
        // get original file From server

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof FileProcessingDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + VideoTranscodingDetails.class);
            return Status.FAILURE;
        }

        FileProcessingDetails fileProcessingDetails = (FileProcessingDetails) details;

        if (StringUtils.isEmpty(fileProcessingDetails.jobId)) {
            LOGGER.error("JobId not present");
            return Status.FAILURE;
        }

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE
                .getById(fileProcessingDetails.jobId);
        if (status == null) {
            LOGGER.error("Job for not jobId : " + fileProcessingDetails.jobId + " present");
            return Status.FAILURE;
        }
        String jobId = status._getStringId();
        try {
            CMDSFile cmdsFile = CMDSFileDAO.INSTANCE.getById(fileProcessingDetails.fileId);
            CMDSFileStorage storage = new CMDSFileStorage();
            try {
                String originalFileName = AbstractEntityFileStorage.computeFileId(cmdsFile.uuid,
                        EntityType.CMDSFILE,
                        FileUtils.getExtensionWithoutDOT(cmdsFile.originalFileName),
                        MediaType.FILE, FileCategory.ORIGINAL, null);
                File file = FileModelUtils.moveFileLocally(storage, originalFileName, cmdsFile,
                        MediaType.FILE);
                if (file == null) {
                    LOGGER.error("File not moved locally for processing: ");
                    return Status.FAILURE;
                }

                if (fileProcessingDetails.encryptIfNeeded) {

                    if (StringUtils.isNotEmpty(cmdsFile.passphrase)) {
                        LOGGER.debug(" passphrase provided so will do encryption");
                        try {
                            FileEncryptor.encrypt(storage, cmdsFile, file, MediaType.FILE);
                            cmdsFile.states.add(FileConversionState.ENCRYPTED);
                            CMDSFileDAO.INSTANCE.updateModel(cmdsFile, Arrays.asList("states"));
                        } finally {

                            EntityOperationStatusDAO.INSTANCE.incCompletion(jobId);
                        }
                    }

                }
                cmdsFile.converted=true;
                CMDSFileDAO.INSTANCE.updateModel(cmdsFile, Arrays.asList("converted"));
                CMDSDocumentManager
                        .generateEventAysc(cmdsFile.userId, cmdsFile, EventActionType.ADD,
                                EventType.INDEX_CMDS_FILE, UserActionType.ADDED, false);

            } catch (VedantuException e) {
                LOGGER.error("Can not encrypt files document", e);
                return Status.FAILURE;
            }

            return Status.SUCCESS;

        } catch (Exception e) {

            LOGGER.error(
                    "Conversion failed for jobId " + jobId + " & eventId " + event._getStringId(),
                    e);
            EntityOperationStatusDAO.INSTANCE.updateErrorCode(jobId,
                    VedantuErrorCode.CONVERSION_FAILED.name());
        }

        return Status.FAILURE;
    }

}
