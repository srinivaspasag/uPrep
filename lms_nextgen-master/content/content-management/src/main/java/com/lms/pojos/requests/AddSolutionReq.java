package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.enums.SolutionType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.Attachment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AddSolutionReq extends AbstractAuthCheckReq implements
		IReverseImageMapperProcessor {
	@NotBlank(message = "qId should not be null")
	public String qId;
	@NotBlank(message = "content should not be null")
	public String content;
	public List<String> answers;
	public Map<String, List<String>> gridAnswer;
	public SolutionType type;
	public List<Attachment> attachments;

	public AddSolutionReq() {
	}

	public AddSolutionReq(String userId, String qid, String content,
						  List<String> answers, Map<String, List<String>> gridAnswer,
						  SolutionType type) {
		this.userId = userId;
		this.qId = qid;
		this.content = content;
		this.answers = answers;
		this.gridAnswer = gridAnswer;
		this.type = type;
	}

	@Override
	public void addImageSrcUrl() {

	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {
		/*content = QuestionManager.removeTempImageSrcAndSaveToFS(
				EntityType.SOLUTION, content, true, "content");*/
	}

}
