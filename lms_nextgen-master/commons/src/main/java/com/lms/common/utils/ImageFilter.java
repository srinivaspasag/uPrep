package com.lms.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ImageFilter extends FileFilterUtils implements FileFilter {
    private static final Logger logger = LoggerFactory.getLogger(ImageFilter.class);

    public final static String JPEG = "jpeg";
    public final static String PNG = "png";
    public final static String GIF = "gif";
    public final static String TIFF = "tiff";
    public final static String BMP = "bmp";
    private final static Map<String, String> imageMap = new HashMap<String, String>();
    static {
        imageMap.put(".jpeg", JPEG);
        imageMap.put(".jpg", JPEG);
        imageMap.put(".png", PNG);
        imageMap.put(".gif", GIF);
        imageMap.put(".tif", TIFF);
        imageMap.put(".tiff", TIFF);
        imageMap.put(".bmp", BMP);
    }

    private String imageType;

    @Override
    public boolean accept(File file) {
        if (null == file) {
            logger.error("will not accept null file");
            return false;
        }
        logger.debug("testing file" + file.getAbsolutePath());
        String fileName = file.getName().toLowerCase();
        int extensionIndex = fileName.lastIndexOf(".");
        String extension = fileName.substring(extensionIndex);
        imageType = imageMap.get(extension);
        return imageType != null;
    }

    public String getImageType() {
        return imageType;
    }

    public static String getJPGType(String imageName) {
        return imageName.substring(0,imageName.indexOf(".")-1)+FileUtils.JPG_EXTENTION;

    }
}