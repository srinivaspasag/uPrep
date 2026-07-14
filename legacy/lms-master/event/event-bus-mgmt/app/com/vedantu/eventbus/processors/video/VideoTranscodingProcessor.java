package com.vedantu.eventbus.processors.video;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.models.event.details.VideoTranscodingDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.media.AudioPresets;
import com.vedantu.commons.entity.media.VideoPresets;
import com.vedantu.commons.entity.media.VideoThumbnailGenerator;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSVideoStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.FileConversionState;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.models.Video;
import com.vedantu.content.utils.FileEncryptor;
import com.vedantu.content.utils.FileModelUtils;
import com.vedantu.eventbus.shell.executors.AVCONVVideoConverter;
import com.vedantu.eventbus.shell.executors.AVProbeVideoDataFetcher;
import com.vedantu.eventbus.shell.executors.MkCleanExecutor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuRecordState;
import com.xuggle.xuggler.Converter;

public class VideoTranscodingProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(VideoTranscodingProcessor.class);

    @SuppressWarnings("unused")
    @Override
    public Status process(IConsumable consumable) {

        long startTime = 0, conversionFinishedTime = 0, encryptionFinishTime = 0, thumbGeneratedFinishTime = 0;
        long originalSize = 0, convertedSize = 0, thumbnailSize =0, encryptedSize =0;
        long videoDuration = 0;
        String orignalFileName = null, convertedFileName = null;

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

        if (details == null || !(details instanceof VideoTranscodingDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + VideoTranscodingDetails.class);
            return Status.FAILURE;
        }

        VideoTranscodingDetails videoTranscodingDetails = (VideoTranscodingDetails) details;

        if (StringUtils.isEmpty(videoTranscodingDetails.jobId)) {
            LOGGER.error("JobId not present");
            return Status.FAILURE;
        }

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getById(
                videoTranscodingDetails.jobId, VedantuRecordState.ACTIVE);
        if (status == null) {
            LOGGER.error("Job for not jobId : " + videoTranscodingDetails.jobId + " present");
            return Status.FAILURE;
        }
        String jobId = status._getStringId();

        LocalFileSystemHandler tempFS = FileSystemFactory.INSTANCE.getTempFS();
        CMDSVideo video = CMDSVideoDAO.INSTANCE.getById(videoTranscodingDetails.videoId);
        CMDSVideoStorage storage = new CMDSVideoStorage();

        try {

            String originalFileName = AbstractEntityFileStorage.computeFileId(video.uuid,
                    EntityType.CMDSVIDEO, FileUtils.getExtensionWithoutDOT(video.originalFileName),
                    MediaType.VIDEO, FileCategory.ORIGINAL, null);
            File inputVideoFile = FileModelUtils.moveFileLocally(storage, originalFileName, video,
                    MediaType.VIDEO);

            // video stats
            startTime = System.currentTimeMillis();
            originalFileName = video.originalFileName;
            originalSize = inputVideoFile.length();

            if (inputVideoFile == null) {
                LOGGER.error("File not moved locally for processing: ");
                return Status.FAILURE;
            }

            AVProbeVideoDataFetcher fetcher = new AVProbeVideoDataFetcher();
            fetcher.fetchInfo(inputVideoFile);
            VideoPresets inputVideoPresets = fetcher.getVideoPresets();

            if (videoTranscodingDetails.generateDuration) {

                LOGGER.debug("Actual duration " + inputVideoPresets.duration);

                video.duration = (long) inputVideoPresets.duration;

                LOGGER.debug("Computed duration in seconds" + video.duration);

                // CMDSVideoDAO.INSTANCE.save(video);
                CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList("duration"));
                EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // 1st step
            }

            if (videoTranscodingDetails.generateThumbnail && inputVideoFile != null
                    && inputVideoFile.exists()) {
                String getTempImageFileName = tempFS.getFilePath(EntityType.VIDEO.name()
                        .toLowerCase(), UUID.randomUUID() + FileUtils.JPG_EXTENTION);

                File thumbnailFile = new File(getTempImageFileName);

                AVCONVVideoConverter convertor = new AVCONVVideoConverter();
                convertor.setMonitorable(false);
                convertor.grabImage(inputVideoFile, thumbnailFile,
                        inputVideoPresets.duration > 100 ? 100 : 10);

                if (thumbnailFile != null && thumbnailFile.exists()) {
                    if (VideoThumbnailGenerator.INSTANCE.generateThumbails(thumbnailFile,
                            video._getStringId())) {
                        video.thumbnail = video._getStringId();
                        thumbnailSize = thumbnailFile.length();
                        // CMDSVideoDAO.INSTANCE.save(video);
                        CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList("thumbnail"));
                        if(!StringUtils.isEmpty(video.globalVideoId)){
                            Video vid = VideoDAO.INSTANCE.getById(video.globalVideoId);
                            vid.thumbnail = video.thumbnail;
                            vid.duration = (long) inputVideoPresets.duration;
                            VideoDAO.INSTANCE.save(vid);
                        }
                    }
                }

                EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // 2nd step
                // video stats
                thumbGeneratedFinishTime = System.currentTimeMillis();
                videoDuration = video.duration;

            }
            File convertedFile = null;
            if (videoTranscodingDetails.generateNewVideo) {

                String newOutputFile = tempFS
                        .getFilePath(
                                EntityType.VIDEO.name().toLowerCase(),
                                video.uuid
                                        + "."
                                        + (StringUtils
                                                .isNotEmpty(videoTranscodingDetails.convertedFileFormat) ? videoTranscodingDetails.convertedFileFormat
                                                : "webm"));
                convertedFile = new File(newOutputFile);

                AVCONVVideoConverter convertor = new AVCONVVideoConverter();
                convertor.setOutputAudioPresets(videoTranscodingDetails.audioPreset);
                convertor.setOutputVideoPresets(videoTranscodingDetails.videoPreset);
                convertor.convert(inputVideoFile, convertedFile);
            } else {
                // not coverting videos
                convertedFile = inputVideoFile;
            }

            // mkclean execution converted file

            if (convertedFile.exists()) {

                LOGGER.debug("-----------------------------------------------------------------------------");
                LOGGER.debug(inputVideoFile.getName()+" File converted, Now about to clean it");
                LOGGER.debug("-----------------------------------------------------------------------------");

                String newOutputFile = tempFS
                        .getFilePath(
                                EntityType.VIDEO.name().toLowerCase(),
                                UUID.randomUUID()
                                        + "."
                                        + (StringUtils
                                                .isNotEmpty(videoTranscodingDetails.convertedFileFormat) ? videoTranscodingDetails.convertedFileFormat
                                                : "webm"));

                File optimizedFile = new File(newOutputFile);

                MkCleanExecutor convertor = new MkCleanExecutor();
                boolean mkCleanFailed = convertor.convert(convertedFile, optimizedFile);
                if (!mkCleanFailed) {
                    return Status.FAILURE;
                }

                video.extension = FileUtils.getExtensionWithoutDOT(optimizedFile.getName());
                video.states.add(FileConversionState.CONVERTED);

                Map<String, String> tags = new HashMap<String, String>();
                tags.put(ConstantsGlobal.ORG_ID, video.contentSrc.id);
                tags.put(ConstantsGlobal.VIDEO_ID, video._getStringId());
                StorageResult result = storage.storeVideo(video.uuid, optimizedFile,
                        FileCategory.CONVERTED, tags, MediaType.VIDEO);
                CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList("extension", "states"));

                EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // 3rd step

                // video stats
                conversionFinishedTime = System.currentTimeMillis();
                convertedFileName = result.fileId;
                convertedSize = convertedFile.length();

                video.converted = true;
                video.size.reset();
                video.size.addOriginal(originalSize);
                video.size.addConverted(convertedSize);
                video.size.addEncrypted(convertedSize);
                video.size.addThumbnail(thumbnailSize);

            }
            CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList("converted"));
            CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList(CMDSVideo.SIZE));

            if (StringUtils.isNotEmpty(video.passphrase)) {
                LOGGER.debug("passphrase provided so will do encryption encryption");
                try {
                    FileEncryptor.encrypt(storage, video, convertedFile, MediaType.VIDEO);
                    video.states.add(FileConversionState.ENCRYPTED);
                    CMDSVideoDAO.INSTANCE.updateModel(video, Arrays.asList("states"));
                    // video stats
                    encryptionFinishTime = System.currentTimeMillis();
                } finally {
                    // CMDSVideoDAO.INSTANCE.save(video);
                    EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // 4th step

                    AbstractCMDSContentManager.generateEventAysc(video.userId, video,
                            EventActionType.ADD, EventType.INDEX_CMDS_VIDEO, UserActionType.ADDED,
                            false);
                }
            }

            if(!StringUtils.isEmpty(video.globalVideoId)){
                Video vid = VideoDAO.INSTANCE.getById(video.globalVideoId);
                vid.states = video.states;
                vid.extension = video.extension;
                VideoDAO.INSTANCE.save(vid);
            }

            LOGGER.debug("video-stats-heading:StartTime,OrginalFileName,convertedFileName,videoDuration,originalSize,convertedSize,conversionFinishedTime,encryptionFinishTime,thumbGeneratedFinishTime");
            LOGGER.debug("video-stats:+" + startTime + "," + video.originalFileName + ","
                    + convertedFileName + "," + videoDuration + "," + originalSize + ","
                    + convertedSize + "," + conversionFinishedTime + "," + encryptionFinishTime
                    + "," + thumbGeneratedFinishTime);

            return Status.SUCCESS;

        } catch (Exception e) {

            LOGGER.error(
                    "Conversion failed for jobId " + jobId + " & eventId " + event._getStringId(),
                    e);
            EntityOperationStatusDAO.INSTANCE.updateErrorCode(jobId,
                    VedantuErrorCode.CONVERSION_FAILED.name());
        } catch (VedantuException e) {
            LOGGER.error(
                    "Conversion failed for jobId " + jobId + " & eventId " + event._getStringId(),
                    e);
            EntityOperationStatusDAO.INSTANCE.updateErrorCode(jobId, e.errorCode.name());
        }

        return Status.FAILURE;
    }

    /**
     * Options can be found at following class {@link http
     * ://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/Converter.java}
     *
     * @param vPresets
     * @param aPresets
     * @param inputFile
     * @param outputFile
     * @return
     */
    public boolean convert(VideoPresets vPresets, AudioPresets aPresets, File inputFile,
            File outputFile) {

        //
        String[] arguments = { "--icontainerformat",
                FileUtils.getExtensionWithoutDOT(inputFile.getName()), inputFile.getAbsolutePath(),
                outputFile.getAbsolutePath() };
        Converter convertor = new Converter();

        Options cliOptions = convertor.defineOptions();
        //
        CommandLine cmdLine;
        try {
            LOGGER.debug("Encoding startTime" + System.currentTimeMillis());
            cmdLine = convertor.parseOptions(cliOptions, arguments);
            convertor.run(cmdLine);
            LOGGER.debug("Encoding entTime" + System.currentTimeMillis());
            return true;
        } catch (ParseException ex) {
            Logger.error(" Conversion failed", ex);

        } catch (RuntimeException ex) {
            Logger.error(" Conversion failed", ex);
        }

        return false;
    }
}
