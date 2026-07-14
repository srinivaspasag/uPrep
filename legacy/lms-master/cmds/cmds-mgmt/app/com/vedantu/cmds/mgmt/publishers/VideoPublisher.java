package com.vedantu.cmds.mgmt.publishers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.media.VideoPresets;
import com.vedantu.commons.entity.media.VideoThumbnailGenerator;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSVideoStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.models.Video;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.search.details.VideoSearchIndexDetails;
import com.vedantu.content.utils.FileModelUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class VideoPublisher extends AbstractCMDSContentManager {

    private static final ALogger       LOGGER   = Logger.of(VideoPublisher.class);

    public static final VideoPublisher INSTANCE = new VideoPublisher();

    private VideoPublisher() {

        super();
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSVIDEO, this);
    }

    @Override
    public void prePublish(SrcEntity content) {

        // TODO Auto-generated method stub

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

        // TODO Auto-generated method stub

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        // get cmds video

        CMDSVideo cmdsVideo = CMDSVideoDAO.INSTANCE.getById(content.id);
        // if (cmdsVideo.published) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }
        if (cmdsVideo.linkType == LinkType.ADDED && StringUtils.isEmpty(cmdsVideo.url)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
        }

        if (cmdsVideo.linkType == LinkType.UPLOADED
                && (StringUtils.isEmpty(cmdsVideo.uuid) || !cmdsVideo.converted)) {
            throw new VedantuException(VedantuErrorCode.NOT_CONVERTED,
                    "video conversion under progress");
        }
        if(cmdsVideo.linkType != LinkType.ADDED){
            LOGGER.debug("----------------------------------------------");
            LOGGER.debug("Duration is ZERO. So Getting duration");
            LOGGER.debug("----------------------------------------------");
            CMDSVideoStorage storage = new CMDSVideoStorage();
            String originalFileName = AbstractEntityFileStorage.computeFileId(cmdsVideo.uuid,
                    EntityType.CMDSVIDEO, FileUtils.getExtensionWithoutDOT(cmdsVideo.originalFileName),
                    MediaType.VIDEO, FileCategory.ORIGINAL, null);
            File inputVideoFile = FileModelUtils.moveFileLocally(storage, originalFileName, cmdsVideo,
                    MediaType.VIDEO);
            AVProbeVideoDataFetcher fetcher = new AVProbeVideoDataFetcher();
            LOGGER.debug("::::::::::::       Before calling fetchInfo");
            fetcher.fetchInfo(inputVideoFile);
            LOGGER.debug("::::::::::::       After calling fetchInfo");
            VideoPresets inputVideoPresets = fetcher.getVideoPresets();
            cmdsVideo.duration = (long) inputVideoPresets.duration;
            if(StringUtils.isEmpty(cmdsVideo.thumbnail)){
                LocalFileSystemHandler tempFS = FileSystemFactory.INSTANCE.getTempFS();
                String getTempImageFileName = tempFS.getFilePath(EntityType.VIDEO.name()
                        .toLowerCase(), UUID.randomUUID() + FileUtils.JPG_EXTENTION);

                File thumbnailFile = new File(getTempImageFileName);

                AVCONVVideoConverter convertor = new AVCONVVideoConverter();
                convertor.setMonitorable(false);
                LOGGER.debug("Input Video Presets :: "+inputVideoPresets.duration);
                convertor.grabImage(inputVideoFile, thumbnailFile,
                        inputVideoPresets.duration > 2 ? "00:00:02" : "00:00:01");
                if (thumbnailFile != null && thumbnailFile.exists()) {
                    if (VideoThumbnailGenerator.INSTANCE.generateThumbails(thumbnailFile,
                            cmdsVideo._getStringId())) {
                        cmdsVideo.thumbnail = cmdsVideo._getStringId();
                        CMDSVideoDAO.INSTANCE.updateModel(cmdsVideo, Arrays.asList("thumbnail"));
                    }
                }
            }
        }



        // create new ILE video

        Video video  = ObjectMapperUtils.convertValue(cmdsVideo, Video.class);

        if (StringUtils.isNotEmpty(cmdsVideo.globalVideoId)) {
            // copy stuff
            video.id = new ObjectId(cmdsVideo.globalVideoId);
        }

        if(video.recordState == VedantuRecordState.TEMPORARY){
            video.published = true;
        }

        else{
        video.setCmdsVideoId(cmdsVideo._getStringId());
        video.description = cmdsVideo.description;
        video.extension = cmdsVideo.extension;
        video.uuid = cmdsVideo.uuid;
        video.thumbnail = cmdsVideo.thumbnail;
        video.linkType = video.linkType;
        video.url = video.url;
        video.scope = Scope.ORG;
        video.published = true;
        video.name = cmdsVideo.name;
        video.duration = cmdsVideo.duration;
        video.converted = cmdsVideo.converted;
        video.states = cmdsVideo.states;
        video.passphrase = cmdsVideo.passphrase;
        video.size= cmdsVideo.size;
        if (video.duration == -1) {
            throw new VedantuException(VedantuErrorCode.DURATION_NOT_FOUND);
         }
        }

        VideoDAO.INSTANCE.save(video);

        // save new ILE video

        cmdsVideo.globalVideoId = video._getStringId();
        cmdsVideo.published = video.published;
        cmdsVideo.publishingInProgress = false;
        CMDSVideoDAO.INSTANCE.save(cmdsVideo);

        // live add global test to search index
        VideoSearchIndexDetails details = new VideoSearchIndexDetails();
        details.fromMongoModel(video);
        addLiveEntityToSearchIndex(details, EntityType.VIDEO, true);

        // create INDEX_VIDEO
        generateEventAysc(userId, cmdsVideo, EventActionType.UPDATE, EventType.INDEX_CMDS_VIDEO,
                UserActionType.UPDATED, false);

        return cmdsVideo;
    }
    // Its not working, Fix it
    public static String getHalfDuration(Long milliseconds) {

        Date dt = new Date(milliseconds/2);
        String format = new SimpleDateFormat("hh:mm:ss").format(dt);
        return format;
    }

}
