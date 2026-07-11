package com.vedantu.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;


public class ImageFilter extends FileFilterUtils implements FileFilter {
	private final static ALogger LOGGER = Logger.of(ImageFilter.class);

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
			LOGGER.error("will not accept null file");
			return false;
		}
		LOGGER.debug("testing file" + file.getAbsolutePath());
		String fileName = file.getName().toLowerCase();
		int extensionIndex = StringUtils.lastIndexOf(fileName, ".");
		String extension = StringUtils.substring(fileName, extensionIndex);
		imageType = imageMap.get(extension);
		return imageType != null;
	}

	public String getImageType() {
		return imageType;
	}

	public static String getJPGType(String imageName) {
		return (StringUtils.substringBeforeLast(imageName, ".") + FileUtils.JPG_EXTENTION);
	}
}
