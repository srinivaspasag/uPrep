package com.vedantu.commons.utils.image;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageFilter;

/**
 * This will generate different images based on sizes requested
 * 
 * @author vikram
 * 
 */
public class ImageGenerator {
	private final static ALogger LOGGER = Logger.of(ImageGenerator.class);

	public static File createImage(File image, ImageSize size) throws Exception {
		return createImage(image, size, image.getName());
	}

	/**
	 * Return file location created image using temporary location
	 * 
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public static File createImage(File image, ImageSize size, String fileName)
			throws Exception {
		LOGGER.info("createImage image: " + image + ", size: " + size
				+ ", fileName: " + fileName);

		ImageFilter imageFilter = new ImageFilter();
		boolean isImage = imageFilter.accept(new File(fileName));
		LOGGER.debug("createImage isImage: " + isImage);

		if (!isImage) {
			return null;
		}

		String imageType = imageFilter.getImageType();
		LOGGER.debug("createImage imageType: " + imageType);

		String imageName = fileName;
		LOGGER.debug("createImage imageName: " + imageName);

		imageName = StringUtils.substring(imageName,
				StringUtils.indexOf(imageName, "."));
		LOGGER.info("Uploaded image name is : " + imageName);
		LOGGER.info("file extention is " + imageType);

		LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
				.getTempFS();

		File generatedFile = tempFileSystemHandler.getNewFile("ImageTemp",
				FileUtils.JPG_EXTENTION_WITHOUT_DOT);

		LOGGER.debug("Creating " + size.name() + " for imgFile "
				+ image.getAbsolutePath() + " as "
				+ generatedFile.getAbsolutePath() + " as width "
				+ size.getWidthPropertyValue() + " & height: "
				+ size.getHeightPropertyValue());

		if (size == ImageSize.ORIGINAL) {

			ImageUtils.createThumbnail(image.getAbsolutePath(),
					generatedFile.getAbsolutePath());
		} else {

			ImageUtils.createThumbnail(image.getAbsolutePath(),
					generatedFile.getAbsolutePath(),
					size.getWidthPropertyValue(), 0);

		}

		return generatedFile;
	}

}
