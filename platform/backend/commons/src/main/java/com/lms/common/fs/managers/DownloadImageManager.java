package com.lms.common.fs.managers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lms.common.utils.ImageUtils;
import com.lms.common.utils.URLUtils;

public class DownloadImageManager {
	private static final Logger logger = LoggerFactory.getLogger(DownloadImageManager.class);

	public static void main(String[] args) {
		downloadImage(
				"http://ts1.mm.bing.net/images/thumbnail.aspx?q=4721638425691156&id=a8730c42ef4145e261ab8124ec05119c&index=newexp",
				"/home/shankar/ssk/dia.png", "png");
		System.out.println("downloaded successfully");
	}

	public static File downloadImage(String url, String toLocation, String imageType) {

		File downloadedFile = null;

		if (!URLUtils.isValidURL(url)) {
			logger.debug("not a valid url : " + url);
			return downloadedFile;
		}

		try {
			URL u = new URL(url);
			long sTime = System.currentTimeMillis();
			Object content = u.getContent();
			/*if (content instanceof URLImageSource) {
				logger.debug("source is an image ");
				Image image = ImageIO.read(u);
				logger.debug("image height : " + image.getHeight(null) + "image width : " + image.getWidth(null));
				ImageUtils.writeImage((BufferedImage) image, toLocation, imageType);

				downloadedFile = new File(toLocation);

			} else {
				logger.debug("conetent type: " + content.getClass());
			}*/
			long eTime = System.currentTimeMillis();
			logger.debug(
					"total time taken to download image from : [URL] : " + url + " is : " + (eTime - sTime) + " ms");
		} catch (MalformedURLException e) {
			logger.error("Can not connect to source URL : " + url, e);
		} catch (IOException e) {
			logger.error("Can not create to output file at: " + toLocation, e);
		}
		return downloadedFile;
	}
}
