package com.lms.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;


public class ImageCropper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCropper.class);

    public static void main(String[] args) throws Exception {
        String inputFileLocation = "/home/shankar/ssk/1_300.jpg";
        String outputFileLocation = "/home/shankar/ssk/1_300_crop.jpg";
        ImageUtils.cropImage(inputFileLocation, outputFileLocation, 300, 300,
                500, 400, "jpeg");
    }

    public static BufferedImage cropImage(BufferedImage img, int cropWidth,
                                          int cropHeight, int cropStartX, int cropStartY) throws Exception {

        BufferedImage cropedImage = null;
        Dimension size = new Dimension(cropWidth, cropHeight);

        Rectangle clip = createRectangle(img, size, cropStartX, cropStartY);

        try {
            int w = clip.width;
            int h = clip.height;

            LOGGER.info("Crop Width " + w);
            LOGGER.info("Crop Height " + h);
            LOGGER.info("Crop Location Cordinates :  " + "(" + clip.x + ","
                    + clip.y + ")");
            cropedImage = img.getSubimage(clip.x, clip.y, w, h);
            LOGGER.info("Image Cropped. New Image Dimension: "
                    + cropedImage.getWidth() + "w X " + cropedImage.getHeight()
                    + "h");
        } catch (RasterFormatException e) {
            LOGGER.error("Raster format error: " + e.getMessage(), e);
        }
        return cropedImage;
    }

    private static Rectangle createRectangle(BufferedImage img, Dimension size,
                                             int clipX, int clipY) throws Exception {

        Rectangle rectangle = null;
        boolean isAreaAdjusted = false;
        if (clipX < 0) {
            clipX = 0;
            isAreaAdjusted = true;
        }
        if (clipY < 0) {
            clipY = 0;
            isAreaAdjusted = true;
        }

        // Checking if the clip area lies outside the rectangle
        if ((size.width + clipX) <= img.getWidth()
                && (size.height + clipY) <= img.getHeight()) {
            // Setting up a clip rectangle when clip area lies within the image.
            rectangle = new Rectangle(size);
            rectangle.x = clipX;
            rectangle.y = clipY;
        } else {
            // Checking if the width of the clip area lies outside the image. If
            // so, making the image width boundary as the clip width.
            if ((size.width + clipX) > img.getWidth()) {
                size.width = img.getWidth() - clipX;
            }
            // Checking if the height of the clip area lies outside the image.
            // If so, making the image height boundary as the clip height.
            if ((size.height + clipY) > img.getHeight()) {
                size.height = img.getHeight() - clipY;
            }

            // Setting up the clip are based on our clip area size adjustment
            rectangle = new Rectangle(size);
            rectangle.x = clipX;
            rectangle.y = clipY;
            isAreaAdjusted = true;
        }
        if (isAreaAdjusted) {
            LOGGER.info("Crop Area Lied Outside The Image."
                    + " Adjusted The Clip Rectangle\n");
        }
        return rectangle;
    }

}
