package com.vedantu.content.pojos.responses.questions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.enums.SolutionType;
import com.vedantu.content.pojos.AttachmentInfo;
import com.vedantu.content.pojos.responses.AbstractContentUserActionRes;

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
		if(CollectionUtils.isNotEmpty(answers)){
		    List<String> newAnswers = new ArrayList<String>();
		    for( String answer : answers){
		        answer = ImageHTMLUtils.addImageSrcUrl(EntityType.SOLUTION, answer);
		        newAnswers.add(answer);
		    }
		    answers= newAnswers;
		}
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {

	}

}
