package com.vedantu.commons.entity.media;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

import javax.imageio.ImageIO;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.utils.FileUtils;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class ImageSnapListener extends MediaListenerAdapter {

    private final static ALogger LOGGER                       = Logger.of(ImageSnapListener.class);
    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.

    public static final double   SECONDS_BETWEEN_FRAMES       = 1;
    public static final long     MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    public boolean               imageGrabbed                 = false;
    public File                  thumbnailImageFile           = null;

    private long                 thumbnailOffsetInSeconds     = 10;

    public ImageSnapListener(long thumbnailOffsetInSeconds) {

        this.thumbnailOffsetInSeconds = thumbnailOffsetInSeconds;
    }

    public void onVideoPicture(IVideoPictureEvent event) {

        System.out.println(event.getStreamIndex() + "  " + event.getTimeStamp());

        // indicate file written
        double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
        if (seconds == thumbnailOffsetInSeconds) {

            thumbnailImageFile = dumpImageToFile(event.getImage());

            this.imageGrabbed = true; // set this var to true once an image is grabbed out of
                                      // the movie.
            LOGGER.debug("at elapsed time of " + seconds + " seconds wrote: "
                    + thumbnailImageFile.getAbsolutePath());
        }

        // System.out.printf("at elapsed time of %6.3f seconds wrote: SOMEFILE\n",seconds);

        // update last write time
        // mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
        //
        // }

    }

    private File dumpImageToFile(BufferedImage image) {

        try {
            LocalFileSystemHandler tempFS = FileSystemFactory.INSTANCE.getTempFS();

            String getTempImageFileName = tempFS.getFilePath(EntityType.VIDEO.name().toLowerCase(),
                    UUID.randomUUID() + FileUtils.JPG_EXTENTION);
            LOGGER.debug("Thumbnail image name is going to be : =====>" + getTempImageFileName);

            thumbnailImageFile = new File(getTempImageFileName);
            ImageIO.write(image, "jpg", thumbnailImageFile);

            return thumbnailImageFile;

        }

        catch (Exception e) {
            LOGGER.error("Could not generate thumbnail file ", e);
            return null;

        }

    }

    public boolean isImageGrabbed() {

        return imageGrabbed;
    }

}
