package com.lms.component;

import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.entity.media.AudioPresets;
import com.lms.common.vedantu.entity.media.VideoPresets;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSVideo;
import com.lms.models.event.search.details.VideoTranscodingDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class CmdsVideoManager extends AbstractContentManager {

    @Autowired
    private EntityOperationStatusRepo entityOperationStatusRepo;

    public void startReprocessingVideo(CMDSVideo cmdsVideo, int bitrate) {

        Map<OperationType, String> operationJobIdMap = new HashMap<OperationType, String>();

        EntityOperationStatus status = new EntityOperationStatus();

        VideoTranscodingDetails details = new VideoTranscodingDetails();

        details.audioPreset = new AudioPresets();
        details.videoPreset = new VideoPresets();
        details.videoPreset.fileExt = FileUtils.WEBM_EXTENTION_WITHOUT_DOT;
        if (bitrate != 0) {

            details.videoPreset.bitrate = bitrate;
        }
        details.videoId = cmdsVideo._getStringId();

        details.generateThumbnail = true;
        details.generateFileSize = true;
        details.generateDuration = true;
        details.generateNewVideo = !FileUtils.getExtensionWithoutDOT(cmdsVideo.originalFileName)
                .equalsIgnoreCase(FileUtils.WEBM_EXTENTION_WITHOUT_DOT);
        if (!StringUtils.isEmpty(cmdsVideo.passphrase)) {
            details.encryptIfNeeded = true;

        }
        status.numOfSteps += details.generateNewVideo ? 1 : 0;
        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;
        status.numOfSteps += details.generateThumbnail ? 1 : 0;
        status.numOfSteps += details.generateFileSize ? 1 : 0;
        status.numOfSteps += details.generateDuration ? 1 : 0;

        if (status.numOfSteps > 0) {
            status.oType = OperationType.VIDEO_CONVERSION;
            status.id = cmdsVideo._getStringId();
            status.type = EntityType.CMDSVIDEO;
            entityOperationStatusRepo.save(status);
            details.jobId = status._getStringId();

            generateEventAysc(cmdsVideo.userId, details, EventType.CONVERT_VIDEO);
            operationJobIdMap.put(status.oType, status._getStringId());
        }
    }
}
