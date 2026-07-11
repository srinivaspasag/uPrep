package com.lms.pojos.requests.questions;

import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.requests.AbstractAddContentBoardReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AddQuestionReq extends AbstractAddContentBoardReq implements
		IReverseImageMapperProcessor {
	@NotBlank(message = "content should not be empty")
	public String content;

	@NotNull
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
	/*	content = QuestionManager.removeTempImageSrcAndSaveToFS(
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
		*/
	}

}
