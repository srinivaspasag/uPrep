package com.lms.common.utils;

import com.lms.common.fs.handlers.FileSystemFactory;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.vedantu.enums.ImageSize;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;

@Setter
@Getter
public class ImageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerator.class);

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
        logger.info("createImage image: " + image + ", size: " + size
                + ", fileName: " + fileName);

        ImageFilter imageFilter = new ImageFilter();
        boolean isImage = imageFilter.accept(new File(fileName));
        logger.debug("createImage isImage: " + isImage);

        if (!isImage) {
            return null;
        }

        String imageType = imageFilter.getImageType();
        logger.debug("createImage imageType: " + imageType);

        String imageName = fileName;
        logger.debug("createImage imageName: " + imageName);

        imageName = imageName.substring(imageName.indexOf("."),imageName.length()-1);
                //StringUtils.indexOf(imageName, "."));
        logger.info("Uploaded image name is : " + imageName);
        logger.info("file extention is " + imageType);

        LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
                .getTempFS();

        File generatedFile = tempFileSystemHandler.getNewFile("ImageTemp",
                FileUtils.JPG_EXTENTION_WITHOUT_DOT);

        logger.debug("Creating " + size.name() + " for imgFile "
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