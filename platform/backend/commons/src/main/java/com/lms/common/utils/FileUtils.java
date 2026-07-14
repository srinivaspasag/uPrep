package com.lms.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public final static String   HTML_EXTENSION             = ".html";
    public final static String   HTML_EXTENSION_WITHOUT_DOT = "html";
    public final static String   HTM_EXTENSION              = ".htm";
    public final static String   HTM_EXTENSION_WITHOUT_DOT  = "htm";

    public static final String   PDF_EXTENSTION_WITHOUT_DOT = "pdf";
    public static final String   PDF_EXTENSTION             = ".pdf";

    public static final String   JPG_EXTENTION              = ".jpg";
    public static final String   JPG_EXTENTION_WITHOUT_DOT  = "jpg";

    public static final String   ZIP_EXTENTION              = ".zip";
    public static final String   ZIP_EXTENTION_WITHOUT_DOT  = "zip";

    public static final String   WEBM_EXTENTION             = ".webm";
    public static final String   WEBM_EXTENTION_WITHOUT_DOT = "webm";

    public static final String   MP4_EXTENTION             = ".mp4";
    public static final String   MP4_EXTENTION_WITHOUT_DOT = "mp4";

    public static final String   CSV_EXTENTION              = ".csv";
    public static final String   CSV_EXTENTION_WITHOUT_DOT  = "csv";

    public static final String   SEPARATOR_DOT              = ".";
    public static final String   SEPARATOR_FWDSLASH         = "/";
    public static final String   SEPARATOR_UNDERSCORE       = "_";
    public static final String   SEPARATOR_HYPHEN           = "-";

    public static String getExtension(File file) {

        return getExtension(file.getName());
    }

    public static String getExtension(String fileName) {

        return fileName.toLowerCase().substring(fileName.lastIndexOf("."));
    }

    public static String getExtensionWithoutDOT(String fileName) {

        return fileName.toLowerCase().substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getFileName(File file) {

        return getFileName(file.getName());
    }

    public static long getFileSize(File file) {
        if( !file.exists()){
            logger.error("File " +file.getAbsolutePath() +" doesnt exist ");
            return 0;
        }
        return file.length();
    }

    public static String getFileName(String fileName) {

        if( fileName.lastIndexOf(".")!=-1){
            return fileName.substring(0);
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static List<String> getTags(String fileName) {

        String[] fileNameParts = fileName.substring(0,fileName.lastIndexOf('.')).split(".");

        return Arrays.asList(fileNameParts);
    }

    public static void deleteFile(String fileName, File file) {

        if (null == file) {
            logger.error("cannot remove null file for fileName: " + fileName);
            return;
        }
        if (!file.exists()) {
            logger.error("cannot remove non-existant file for fileName: " + fileName);
            return;
        }
        if (!file.isFile()) {
            logger.error("cannot remove non-file file for fileName: " + fileName);
            return;
        }
        logger.debug("removing file: " + fileName + " stored at: " + file.getAbsolutePath());
        boolean deleted = file.delete();
        logger.debug("deleted: " + deleted);
    }

    public static boolean moveFile(File srcFile, File destFile) {

        try {
            Path move = Files.move(srcFile.toPath().getFileName(), destFile.toPath().getFileName());

            FileUtils.moveFile(srcFile, destFile);
        } catch (IOException e) {
            logger.debug("can not move file from :  " + srcFile.getAbsolutePath() + " "
                    + destFile.getAbsolutePath());
            return false;
        }
        return true;

    }
}
