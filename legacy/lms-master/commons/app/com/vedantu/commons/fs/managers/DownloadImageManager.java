package com.vedantu.commons.fs.managers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import play.Logger;
import play.Logger.ALogger;
import sun.awt.image.URLImageSource;

import com.vedantu.commons.utils.URLUtils;
import com.vedantu.commons.utils.image.ImageUtils;

public class DownloadImageManager {
	private static ALogger LOGGER = Logger.of(DownloadImageManager.class);
    public static void main(String[] args) {
        downloadImage(
                "http://ts1.mm.bing.net/images/thumbnail.aspx?q=4721638425691156&id=a8730c42ef4145e261ab8124ec05119c&index=newexp",
                "/home/shankar/ssk/dia.png", "png");
        System.out.println("downloaded successfully");
    }

    public static File downloadImage(String url, String toLocation,
            String imageType) {

    	File downloadedFile = null;
    
        if (!URLUtils.isValidURL(url)) {
        	 LOGGER.debug("not a valid url : " + url);
            return downloadedFile;
        }

        try {
            URL u = new URL(url);
            long sTime = System.currentTimeMillis();
            Object content = u.getContent();
            if (content instanceof URLImageSource) {
            	LOGGER.debug("source is an image ");
                Image image = ImageIO.read(u);
                LOGGER.debug("image height : " + image.getHeight(null)
                        + "image width : " + image.getWidth(null));
                ImageUtils.writeImage((BufferedImage) image, toLocation,
                        imageType);
               
                downloadedFile = new File( toLocation );
               
            } else {
            	 LOGGER.debug("conetent type: " + content.getClass());
            }
            long eTime = System.currentTimeMillis();
            LOGGER.debug("total time taken to download image from : [URL] : "
                            + url + " is : " + (eTime - sTime) + " ms");
        } catch (MalformedURLException e) {
        	LOGGER.error("Can not connect to source URL : " + url ,e );
        } catch (IOException e) {
        	LOGGER.error("Can not create to output file at: " + toLocation ,e );
         }
        return downloadedFile;
    }
}
