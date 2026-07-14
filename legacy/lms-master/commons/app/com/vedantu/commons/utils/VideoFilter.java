package com.vedantu.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

public class VideoFilter extends FileFilterUtils implements FileFilter {

    public static final VideoFilter          INSTANCE          = new VideoFilter();
    private final static ALogger             LOGGER            = Logger.of(VideoFilter.class);
    public final static String               MP4               = "mp4";
    public final static String               WEBM              = "webm";

    public final static long                 MAXIMUM_FILE_SIZE = 1 * 1024 * 1024 * 1024;

    private final static Map<String, String> videoFileTypeMap  = new HashMap<String, String>();
    static {

        videoFileTypeMap.put(".mp4", MP4);
        videoFileTypeMap.put(".webm", WEBM);

    }

    private VideoFilter() {

    }

    private String imageType;

    @Override
    public boolean accept(File file) {

        if (null == file) {
            LOGGER.error("will not accept null file");
            return false;
        }
        LOGGER.debug("testing file" + file.getAbsolutePath());
        String fileName = file.getName().toLowerCase();
        int extensionIndex = StringUtils.lastIndexOf(fileName, ".");
        String extension = StringUtils.substring(fileName, extensionIndex);
        imageType = videoFileTypeMap.get(extension);

        return (imageType != null && file.length() < MAXIMUM_FILE_SIZE);

    }

    public String getImageType() {

        return imageType;
    }
}
