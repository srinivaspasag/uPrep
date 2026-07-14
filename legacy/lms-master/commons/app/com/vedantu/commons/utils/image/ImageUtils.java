package com.vedantu.commons.utils.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author Shankar
 */
public class ImageUtils {

    private final static ALogger LOGGER                    = Logger.of(ImageUtils.class);

    public static final String   JPG_EXTENTION_WITHOUT_DOT = "jpg";

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {

        Map<String, Object> interpolationHint = new HashMap<String, Object>();
        interpolationHint.put("nn", RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        interpolationHint.put("bilinear", RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        interpolationHint.put("bicubic", RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        Map<String, Object> renderingHint = new HashMap<String, Object>();
        renderingHint.put("def", RenderingHints.VALUE_RENDER_DEFAULT);
        renderingHint.put("q", RenderingHints.VALUE_RENDER_QUALITY);
        renderingHint.put("s", RenderingHints.VALUE_RENDER_SPEED);

        for (Map.Entry<String, Object> i : interpolationHint.entrySet()) {
            for (Map.Entry<String, Object> r : renderingHint.entrySet()) {
                String outputFilePath = "/home/shankar/ssk/out/snapshot_thumb_" + i.getKey() + "_"
                        + r.getKey() + ".jpg";
                System.out.println("out : " + outputFilePath);
                long start = System.currentTimeMillis();
                new ImageUtils().createThumbnail("/home/shankar/ssk/snapshot.jpg", outputFilePath,
                        100, 0, i.getValue(), r.getValue());
                long end = System.currentTimeMillis();
                System.out.println("total time take in ms : " + (end - start));
            }
        }

    }

    public static boolean cropImage(URL imageUrl, String ouputImageLocation, int cropWidth,
            int cropHeight, int cropStartX, int cropStartY, String outputImageType) {

        BufferedImage srcImage = readImage(imageUrl);
        return cropImage(srcImage, ouputImageLocation, cropWidth, cropHeight, cropStartX,
                cropStartY, outputImageType);
    }

    public static boolean cropImage(String inputImageLocation, String ouputImageLocation,
            int cropWidth, int cropHeight, int cropStartX, int cropStartY, String outputImageType) {

        BufferedImage srcImage = readImage(inputImageLocation);
        return cropImage(srcImage, ouputImageLocation, cropWidth, cropHeight, cropStartX,
                cropStartY, outputImageType);
    }

    public static boolean cropImage(BufferedImage srcImage, String ouputImageLocation,
            int cropWidth, int cropHeight, int cropStartX, int cropStartY, String outputImageType) {

        boolean isCropped = false;
        if (srcImage == null) {
            LOGGER.error("some error on reading the input file");
            return isCropped;
        }
        if (StringUtils.isEmpty(outputImageType)) {
            outputImageType = "jpg";
        }
        try {
            BufferedImage croppedImage = ImageCropper.cropImage(srcImage, cropWidth, cropHeight,
                    cropStartX, cropStartY);
            if (croppedImage != null) {
                writeImage(croppedImage, ouputImageLocation, outputImageType);
                isCropped = true;
            }
        } catch (Exception e) {
            LOGGER.error("error on croping the inputImage : " + srcImage.getSource(), e);
        }
        return isCropped;

    }

    public void generateThumbnail(String imgFile, String thumbFile, int width, int height)
            throws IOException {

        String imageOutput = "JPEG";
        File inFile = new File(imgFile);
        InputStream is = new FileInputStream(inFile);
        BufferedImage bufferedImage = ImageIO.read(is);

        // Calculate the new Height if not specified
        int calcHeight = height > 0 ? height : (width * bufferedImage.getHeight() / bufferedImage
                .getWidth());
        // Write the image
        ImageIO.write(createResizedCopy(bufferedImage, width, calcHeight), imageOutput, new File(
                thumbFile));
        if (is != null) {
            is.close();
        }
    }

    BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight) {

        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public static void createThumbnail(String imgFile, String thumbFile) throws Exception {

        BufferedImage image = ImageIO.read(new File(imgFile));
        BufferedImage thumbImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        final FileOutputStream fileOutPut = new FileOutputStream(thumbFile);
        BufferedOutputStream out = new BufferedOutputStream(fileOutPut);
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            int quality = 90;
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) quality / 100.0f);
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(thumbImage, null, null), param);
            writer.dispose();
            ios.close();
        } finally {
            fileOutPut.close();
            out.close();
            image.flush();
            thumbImage.flush();
            graphics2D.finalize();
        }
    }

    public static void createThumbnail(String imgFile, String thumbFile, int thumbWidth,
            int thumbHeight) throws Exception {

        createThumbnail(imgFile, thumbFile, thumbWidth, thumbHeight,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static void createThumbnail(String imgFile, String thumbFile, int thumbWidth,
            int thumbHeight, Object interpolationHint, Object renderingHint) throws Exception {

        BufferedImage image = ImageIO.read(new File(imgFile));
        double thumbRatio = 0;
        // Calculate the new Height if not specified
        thumbHeight = thumbHeight > 0 ? thumbHeight : (thumbWidth * image.getHeight(null) / image
                .getWidth(null));

        thumbRatio = (double) thumbWidth / (double) thumbHeight;

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }
        /**
         * for higher quality scale down the image in multiple steps
         * **/
        do {
            if (imageWidth > thumbWidth) {
                imageWidth /= 2;
                if (imageWidth < thumbWidth) {
                    imageWidth = thumbWidth;
                }
            }

            imageHeight = (int) (imageWidth / imageRatio);
            System.out.println("imageWidth : " + imageWidth + ", imageHeight : " + imageHeight);

            BufferedImage thumbImage = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();

            graphics2D.setComposite(AlphaComposite.Src);
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, renderingHint);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2D.drawImage(image, 0, 0, imageWidth, imageHeight, null);

            image = thumbImage;

        } while (imageWidth > thumbWidth);

        BufferedImage thumbImage = (BufferedImage) image;

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(thumbFile));
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        int quality = 90;
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality((float) quality / 100.0f);
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(thumbImage, null, null), param);
        writer.dispose();
        ios.close();
        out.close();
    }

    public static BufferedImage readImage(String imgInputLocation) {

        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(imgInputLocation));
            LOGGER.info("Input Image Read. Image Dimension: " + img.getWidth() + "w X "
                    + img.getHeight() + "h");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return img;
    }

    public static BufferedImage readImage(URL imgURL) {

        BufferedImage img = null;
        try {
            img = ImageIO.read(imgURL);
            LOGGER.info("Input Image Read. Image Dimension: " + img.getWidth() + "w X "
                    + img.getHeight() + "h");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return img;
    }

    public static void writeImage(BufferedImage img, String imgOutputLocation, String extension) {

        try {
            BufferedImage bi = img;
            File outputfile = new File(imgOutputLocation);
            if (bi != null && outputfile != null) {
                ImageIO.write(bi, extension, outputfile);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
