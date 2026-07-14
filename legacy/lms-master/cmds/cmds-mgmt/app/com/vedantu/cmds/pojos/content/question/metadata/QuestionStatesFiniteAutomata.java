package com.vedantu.cmds.pojos.content.question.metadata;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.pojos.content.question.EntireQuestion;

public class QuestionStatesFiniteAutomata {
	private static ALogger			LOGGER		= Logger.of(QuestionStatesFiniteAutomata.class);
	
//	private static final String		l2pPath		= Play.application().configuration().
//														getString("l2p.dir")
//														+ (Play.application().configuration()
//																.getString(
//																		"l2p.dir")
//																.endsWith(
//																		File.separator) ? ""
//																: File.separator)
//														+ "./l2p";


	public static String getHTMLOutputFromLatex(String origValue,
			EntireQuestion q, String type, boolean inHTML, boolean inImgBase64) {
		
		// TODO commented based on Shankars Guidance as whole following code is not being used
//		int found = 1;

//		if (!inHTML) {
//			// if (StringUtils.isNotEmpty(origValue)) {
//			// origValue = StringUtils.replace(
//			// StringUtils.replace(origValue, "\\[",
//			// "<span style=\"margin: 1em 0; text-align:left; font-size: 14px;\">\\(\\displaystyle"),
//			// "\\]",
//			// "\\)</span>");
//			// }
//			// LOGGER.info("original value: " + origValue);
//		} else {
//			while (found != -1) {
//				int previousEndIndex = 0;
//				int startIndex = origValue.indexOf("\\[", previousEndIndex);
//				int endIndex = origValue.indexOf("\\]", previousEndIndex);
//
//				if (inHTML) {
//					String v = null;
//					if (startIndex != -1 && endIndex != -1) {
//						v = origValue.substring(startIndex, endIndex);
//						// LOGGER.info("v:::" + v);
//						String randomUUID = UUID.randomUUID().toString();
//						if (type.equals("options")) {
//							q.formattedOptions.uuidImages.add(randomUUID);
//							q.formattedOptions.originalOptions.add(origValue);
//						} else if (type.equals("text")) {
//							q.formattedQuestion.uuidImages.add(randomUUID);
//							q.formattedQuestion.originalText += origValue;
//						}
//						String latex = "$"
//								+ origValue.substring(startIndex + 2, endIndex)
//								+ "$";
//
//						String finalOutput = "http://localhost/img/"
//								+ randomUUID + "." + "jpg";
//
//						String trgtFileName = Play.application().configuration()
//								.getString("question.img")
//								+ "/"
//								+ randomUUID
//								+ "." + "jpg";
//
//						String command = "./l2p " + trgtFileName + " " + "\""
//								+ latex + "\"";
//						// LOGGER.info("Final latex:::" + latex);
//						if (getFinalHTMLName(trgtFileName, command,
//								finalOutput, latex)) {
//							v = "<img src=\"" + finalOutput + "\"/>";
//							// LOGGER.info(origValue);
//						}
//						// LOGGER.info("previous end index:::" +
//						// previousEndIndex);
//						// LOGGER.info("start index:::" + startIndex);
//						// LOGGER.info("end index:::" + endIndex);
//						// LOGGER.info("original string:::" + origValue);
//						origValue = origValue.substring(previousEndIndex,
//								startIndex)
//								+ v
//								+ origValue.substring(endIndex + 2);
//						previousEndIndex = endIndex;
//						// LOGGER.info("original string:::" + origValue);
//						// LOGGER.info("previous end index:::" +
//						// previousEndIndex);
//					} else {
//						found = -1;
//					}
//				}
//			}
//		}
		return origValue;
	}

	public static List<Integer> getStartingIndexesOfRequiredSubstring(
			String origValue) {
		List<Integer> startIndexesToReturn = new ArrayList<Integer>();
		LOGGER.info("orig string" + " " + origValue);
		int previousEndIndex = 0;
		int startIndex = origValue.indexOf("\\[", previousEndIndex);
		int endIndex = origValue.indexOf("\\]", previousEndIndex + startIndex);

		while (startIndex < origValue.length() && startIndex > -1
				&& endIndex < origValue.length() && endIndex > -1) {
			startIndexesToReturn.add(startIndex);
			previousEndIndex = startIndex;
			LOGGER.info("previous index:::" + previousEndIndex);
			startIndex = origValue.indexOf("\\[", previousEndIndex);
			endIndex = origValue.indexOf("\\]", previousEndIndex + startIndex);
		}

		return startIndexesToReturn;
	}

	public static List<Integer> getEndIndexesOfRequiredSubstring(
			String origValue) {
		List<Integer> endIndexesToReturn = new ArrayList<Integer>();

		int previousEndIndex = 0;
		int startIndex = origValue.indexOf("\\[", previousEndIndex);
		int endIndex = origValue.indexOf("\\]", previousEndIndex);
		while (startIndex < origValue.length() && startIndex != -1
				&& endIndex < origValue.length() && endIndex != -1) {
			endIndexesToReturn.add(endIndex);
			previousEndIndex = startIndex;
			startIndex = origValue.indexOf("\\[", previousEndIndex);
			endIndex = origValue.indexOf("\\]", previousEndIndex + startIndex);
		}

		return endIndexesToReturn;
	}

//	public static boolean getFinalHTMLName(String trgtFileName,
//			String command, String finalOutput, String latex) {
//		LOGGER.info("latex1::::::" + latex);
//		// LOGGER.info("latex2::::::"+latex);
//		String inputOp = "-i";
//		String outputOp = "-o";
//		String[] cmdArray = { l2pPath, inputOp, latex, outputOp, trgtFileName };
//		LOGGER.info("l2p::::::" + l2pPath);
//		ProcessBuilder pb = new ProcessBuilder(cmdArray);
//		// List<String> ls = pb.command();
//		// for(int i=0;i<ls.size();i++)
//		// {
//		// LOGGER.info("args:::"+i+" "+ls.get(i));
//		// }
//		pb.redirectErrorStream(true);
//		int exitStatus = -1;
//		try {
//			exitStatus = pb.start().waitFor();
//		} catch (Exception e) {
//
//			LOGGER.error("", e);
//		}
//		return exitStatus == 0;
//	}
}
