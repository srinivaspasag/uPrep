package com.lms.pojos.responce.questions;

import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.SolutionType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.AttachmentInfo;
import com.lms.pojos.responce.AbstractContentUserActionRes;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetSolutionRes extends AbstractContentUserActionRes implements
		IListResponseObj, IReverseImageMapperProcessor {

	public String qId;
	public String userId;
	public String content;
	public SolutionType type;
	public List<String> answers;
	public boolean verified;
	public List<AttachmentInfo> attachmentsInfo;

	public GetSolutionRes(String id, int upVotes, int views, int followers,
						  int comments, boolean voted, String qid, String userId,
						  String content, SolutionType type, List<String> answers,
						  boolean verified, long timeCreated, long lastUpdated, List<AttachmentInfo> attachmentsInfo) {
		super(id, upVotes, views, followers, comments, timeCreated,
				lastUpdated, voted, null, null);
		this.qId = qid;
		this.userId = userId;
		this.content = content;
		this.type = type;
		this.answers = answers;
		this.verified = verified;
		this.attachmentsInfo = attachmentsInfo;
	}

	@Override
	public String _getEntityId() {
		return id;
	}

	@Override
	public void addImageSrcUrl() {
		content = ImageHTMLUtils.addImageSrcUrl(EntityType.SOLUTION, content);
		if (CollectionUtils.isNotEmpty(answers)) {
			List<String> newAnswers = new ArrayList<String>();
			for (String answer : answers) {
				answer = ImageHTMLUtils.addImageSrcUrl(EntityType.SOLUTION, answer);
				newAnswers.add(answer);
			}
			answers = newAnswers;
		}
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {

	}

}
