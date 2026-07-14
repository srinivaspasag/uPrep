package com.lms.utils;

import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.models.CMDSQuestion;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CMDSImageUtil {
    private static final String DEFAULT_IMAGE_TYPE = "jpg";
    public static String ORIGINAL_DIR = "original";
    @Value("${util.temp_dir}")
    public String TEMP_DIR;
    @Value("${question.img}")

    public String QR_IMG_DIR;

    //
    // public static void copyImageFileToPermanentLocation(String imageLocation,
    // String permanentImageLocation) throws IOException {
    // File tempImage = new File(imageLocation);
    // if (tempImage != null && tempImage.exists()) {
    // File destImageDir = new File(permanentImageLocation)
    // .getParentFile();
    // if (!destImageDir.exists()) {
    // destImageDir.mkdirs();
    // }
    // FileUtils.copyFileToDirectory(tempImage, destImageDir, true);
    // }
    // }
    //
    public static String toImageUrl(String randomUUID) {
        return "<img  src=\""
                + ImageDisplayURLUtil.getQuestionEmbedTempImageURL(randomUUID
                + "." + DEFAULT_IMAGE_TYPE) + "\" />";
    }

    public static String toImageUrl(String randomUUID, String questionSetId) {
        return "<img  src=\""
                + ImageDisplayURLUtil.getQRQuestionEmbedImage(randomUUID + "."
                + DEFAULT_IMAGE_TYPE, questionSetId) + "\" />";
    }

    public static String getEmbededHtml(String url) {
        return "<img  src=\""
                + url + "\" />";
    }

    //
    // public static DBObject convertUuidToImageUrl(DBObject questionDbObject)
    // throws Exception {
    // QRQuestion question = MongoMapper.convertValue(questionDbObject,
    // QRQuestion.class);
    // question.convertUuidsToImageUrls(true, question.questionSetId);
    // questionDbObject.put("solutionInfo", question.solutionInfo);
    // questionDbObject.put("questionBody", question.questionBody);
    // return questionDbObject;
    // }
    //
    // public static DBObject convertUuidToImageUrl(QRQuestion question)
    // throws Exception {
    // DBObject questionDbObject = ObjectMapperUtil.convertValue(question,
    // BasicDBObject.class);
    // question.convertUuidsToImageUrls(true, question.questionSetId);
    // questionDbObject.put("solutionInfo", question.solutionInfo);
    // questionDbObject.put("questionBody", question.questionBody);
    // return questionDbObject;
    // }
    //
    public static void convertImageUrlToUuidAndSaveImage(CMDSQuestion question)
            throws IOException {
        convertImageUrlToUuidAndSaveImage(question, false, false);
    }

    //
    public static void convertImageUrlToUuidAndSaveImage(CMDSQuestion question,
                                                         boolean saveImages, boolean permanentImgUrl) throws IOException {
        if (question != null) {
            question.convertImageUrlToUuids(saveImages, permanentImgUrl,
                    question.questionSetId);
        }
    }

    public String getTempImageFile(String randomUUID) {
        return getTempImageFile(randomUUID, DEFAULT_IMAGE_TYPE);
    }

    public String getTempImageFile(String randomUUID, String imgType) {
        return TEMP_DIR + File.separator + randomUUID + "." + imgType;
    }

    public String getSaveToImageFileDir(String questionSetId) {
        return QR_IMG_DIR + File.separator + questionSetId + File.separator
                + "img";
    }

    public String getSaveToDocumentFileDir(String questionSetId) {
        return QR_IMG_DIR + File.separator + questionSetId + File.separator
                + ORIGINAL_DIR;
    }

    public String getSaveToImageFile(String questionSetId,
                                     String randomUUID) {
        return getSaveToImageFile(questionSetId, randomUUID, DEFAULT_IMAGE_TYPE);
    }

    public String getSaveToImageFile(String questionSetId,
                                     String randomUUID, String imgType) {
        return getSaveToImageFileDir(questionSetId) + File.separator
                + randomUUID + "." + imgType;
    }

    public void moveTempImageFileToPermanentLocation(
            String tempImageLocation, String permanentImageLocation)
            throws IOException {
        File tempImage = new File(tempImageLocation);
        if (tempImage != null && tempImage.exists()) {
            File destImageDir = new File(permanentImageLocation)
                    .getParentFile();
            if (!destImageDir.exists()) {
                destImageDir.mkdirs();
            }
            FileUtils.moveFileToDirectory(tempImage, destImageDir, true);
        }
    }

    public void convertUuidsToImageUrls(List<CMDSQuestion> allQuestions,
                                        boolean permanentImgUrl) throws Exception {
        for (CMDSQuestion q : allQuestions) {
            q.convertUuidsToImageUrls(permanentImgUrl, q.questionSetId);
            // convertUuidToImageUrl(q, permanentImgUrl);
        }
    }
    //
    // public static void convertImageUrlToUuidAndSaveImages1(
    // List<QRQuestion> allQuestions) throws IOException {
    // for (QRQuestion q : allQuestions) {
    // convertImageUrlToUuidAndSaveImage(q);
    // }
    // }


}
