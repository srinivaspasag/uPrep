package com.vedantu.commons.entity.media;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.entity.storage.VideoEntityFileStorage;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.image.ImageGenerator;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

public class VideoThumbnailGenerator {

    private final static ALogger                LOGGER            = Logger.of(VideoThumbnailGenerator.class);

    public static final VideoThumbnailGenerator INSTANCE          = new VideoThumbnailGenerator();

    private long                                imageGrabbingTime = 10;                                      // in
                                                                                                              // seconds

    private long                                videoDuration;                                               // in
                                                                                                              // seconds

    private VideoThumbnailGenerator() {

    }

    public VideoThumbnailGenerator(long videoDuration) {

        if (videoDuration > 100) {
            this.imageGrabbingTime = 100;

        }
        this.videoDuration = videoDuration;
    }

    public File generateFirstFrame(String requestId, String inputFile) {

        long startTime = System.currentTimeMillis();
        long stopTime = 0L;

        IMediaReader mediaReader = ToolFactory.makeReader(inputFile);

        // stipulate that we want BufferedImages created in BGR 24bit color
        // space

        try {
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            ImageSnapListener isListener = new ImageSnapListener(imageGrabbingTime);

            mediaReader.addListener(isListener);

            // read out the contents of the media file and
            // dispatch events to the attached listener

            while (!isListener.isImageGrabbed()) {
                mediaReader.readPacket();

            }
            mediaReader.close();
            /*
             *
             * while (mediaReader.readPacket() == null) ;
             */
            // mediaReader.readPacket();
            stopTime = System.currentTimeMillis();
            return isListener.thumbnailImageFile;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(" RequestId " + requestId + "Total Time: " + (stopTime - startTime));
        return null;
    }

    public boolean generateThumbails(File frameFile, String videoId) {

        try {
            VideoEntityFileStorage picStorage = new VideoEntityFileStorage();
            Map<String, String> tags = new HashMap<String, String>();
            tags.put(EntityType.CMDSVIDEO.name().toLowerCase(), videoId);

            StorageResult picStorageResult = null;
            for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                    ImageSize.EXTRA_SMALL }) {
                File convertedFile = ImageGenerator.createImage(frameFile, imageSize,
                        frameFile.getName());
                picStorageResult = picStorage.storeImage(videoId, convertedFile,
                        FileCategory.CONVERTED, imageSize, null);
                LOGGER.debug("generateThumbails :: "+picStorageResult.toString());

                FileUtils.deleteFile(convertedFile.getName(), convertedFile);
            }
        } catch (Exception exception) {
            LOGGER.debug(" Could not create thumbnail " + exception);
            return false;
        } catch (EntityFileStorageException e) {
            LOGGER.error(" Could not create thumbnail ", e);
            return false;
        }
        return true;
    }

}
