package com.vedantu.cmds.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import play.Play;

import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.commons.utils.ImageDisplayURLUtil;

public class CMDSImageUtil {

	public static final String	TEMP_DIR			= Play.application()
															.configuration()
															.getString(
																	"util.temp_dir")
															+ File.separator
															+ "questions";
	public static final String	QR_IMG_DIR			= Play.application()
															.configuration()
															.getString(
																	"question.img");
	public static final String	ORIGINAL_DIR		= "original";
	private static final String	DEFAULT_IMAGE_TYPE	= "jpg";

	public static String getTempImageFile(String randomUUID) {
		return getTempImageFile(randomUUID, DEFAULT_IMAGE_TYPE);
	}

	public static String getTempImageFile(String randomUUID, String imgType) {
		return TEMP_DIR + File.separator + randomUUID + "." + imgType;
	}

	public static String getSaveToImageFileDir(String questionSetId) {
		return QR_IMG_DIR + File.separator + questionSetId + File.separator
				+ "img";
	}

	public static String getSaveToDocumentFileDir(String questionSetId) {
		return QR_IMG_DIR + File.separator + questionSetId + File.separator
				+ ORIGINAL_DIR;
	}

	public static String getSaveToImageFile(String questionSetId,
			String randomUUID) {
		return getSaveToImageFile(questionSetId, randomUUID, DEFAULT_IMAGE_TYPE);
	}

	public static String getSaveToImageFile(String questionSetId,
			String randomUUID, String imgType) {
		return getSaveToImageFileDir(questionSetId) + File.separator
				+ randomUUID + "." + imgType;
	}

	public static void moveTempImageFileToPermanentLocation(
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

	public static void convertUuidsToImageUrls(List<CMDSQuestion> allQuestions,
			boolean permanentImgUrl) throws Exception {
		for (CMDSQuestion q : allQuestions) {
			q.convertUuidsToImageUrls(permanentImgUrl, q.questionSetId);
			// convertUuidToImageUrl(q, permanentImgUrl);
		}
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
	//
	// public static void convertImageUrlToUuidAndSaveImages1(
	// List<QRQuestion> allQuestions) throws IOException {
	// for (QRQuestion q : allQuestions) {
	// convertImageUrlToUuidAndSaveImage(q);
	// }
	// }

}