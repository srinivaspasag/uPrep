package com.vedantu.eventbus.processors.video;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.CMDSVideoStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.content.event.details.UploadingVideoDetails;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuRecordState;

public class UploadVideoProcessor extends ChainedProcessors implements IProcessor {

    private static final ALogger LOGGER = Logger.of(UploadVideoProcessor.class);

    public UploadVideoProcessor() {

    }

    @Override
    public Status process(IConsumable consumable) {

        // TODO Auto-generated method stub
        // check if file exists locally
        // get original file From server
        // check videoPresets if not present then go for audio

        // check AudioPresets if not present thn return

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof UploadingVideoDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + UploadingVideoDetails.class);
            return Status.FAILURE;
        }

        UploadingVideoDetails uploadingVideoDetails = (UploadingVideoDetails) details;

        if (StringUtils.isEmpty(uploadingVideoDetails.jobId)) {
            LOGGER.error("JobId not present");
            return Status.FAILURE;
        }

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE
                .getById(uploadingVideoDetails.jobId);
        if (status == null) {
            LOGGER.error("Job for not jobId : " + uploadingVideoDetails.jobId + " present");
            return Status.FAILURE;
        }

        CMDSVideoStorage storage = (CMDSVideoStorage) EntityStorageFactory.INSTANCE
                .get(uploadingVideoDetails.type);
        CMDSVideo cmdsVideo = CMDSVideoDAO.INSTANCE.getById(uploadingVideoDetails.id,
                VedantuRecordState.TEMPORARY);
        LOGGER.debug(" CMDSVideo " + cmdsVideo);
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("orgId", cmdsVideo.contentSrc.id);
        tags.put(ConstantsGlobal.ENTITY_TYPE, EntityType.CMDSVIDEO.name());
        File localFile = new File(uploadingVideoDetails.locationOnSrc);
        StorageResult result = null;
        try {
            result = storage.storeVideo(cmdsVideo.uuid, localFile, FileCategory.ORIGINAL, tags,
                    MediaType.VIDEO);
            cmdsVideo.stored = true;
            CMDSVideoDAO.INSTANCE.save(cmdsVideo);
            EntityOperationStatusDAO.INSTANCE.incCompletion(uploadingVideoDetails.jobId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            status.errorCode = VedantuErrorCode.UPLOAD_ERROR.name();
            EntityOperationStatusDAO.INSTANCE.save(status);
            return Status.FAILURE;
        } catch (EntityFileStorageException e) {
            LOGGER.error(e.getMessage(), e);
            status.errorCode = VedantuErrorCode.UPLOAD_ERROR.name();
            EntityOperationStatusDAO.INSTANCE.save(status);
            return Status.FAILURE;
        }
        return Status.SUCCESS;
    }
}
