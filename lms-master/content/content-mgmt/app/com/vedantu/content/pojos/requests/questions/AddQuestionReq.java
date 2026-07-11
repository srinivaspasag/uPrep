package com.vedantu.content.pojos.requests.questions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.pojos.requests.AbstractAddContentBoardReq;

public class AddQuestionReq extends AbstractAddContentBoardReq implements
		IReverseImageMapperProcessor {

	@Required
	public String content;

	@Required
	public QuestionType type;
	public List<String> options;

	public String source;
	public String solution;
	public Map<String, List<String>> matrix;

	public List<String> answers;
	public Map<String, List<String>> matrixAnswer;

	public Difficulty difficulty;

	@Override
	public void addImageSrcUrl() {
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {
		content = QuestionManager.removeTempImageSrcAndSaveToFS(
				EntityType.QUESTION, content, true, "content");
		if (options != null) {
			List<String> newOptions = new ArrayList<String>();
			for (String option : options) {
				option = QuestionManager.removeTempImageSrcAndSaveToFS(
						EntityType.QUESTION, option, true, "content");
				newOptions.add(option);
			}
			options = newOptions;
		}
		if (matrix != null) {
			Map<String, List<String>> newMatrix = new LinkedHashMap<String, List<String>>();
			for (Entry<String, List<String>> entry : matrix.entrySet()) {
				for (String option : entry.getValue()) {
					option = QuestionManager.removeTempImageSrcAndSaveToFS(
							EntityType.QUESTION, option, true, "content");
					if (newMatrix.get(entry.getKey()) == null) {
						newMatrix.put(entry.getKey(), new ArrayList<String>());
					}
					newMatrix.get(entry.getKey()).add(option);
				}
			}
			matrix = newMatrix;
		}
	}

}
