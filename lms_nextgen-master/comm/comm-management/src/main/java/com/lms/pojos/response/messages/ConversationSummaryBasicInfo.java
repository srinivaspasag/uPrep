package com.lms.pojos.response.messages;

import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.ConversationStatus;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.user.vedantu.user.pojo.UserInfo;

import java.io.IOException;

public class ConversationSummaryBasicInfo extends ModelBasicInfo
		implements IListResponseObj, IReverseImageMapperProcessor {

	public String orgId;

	public UserInfo userInfo;
	public String conversationId;
	public String firstMessageId;

	// synchronized content -- starts
	public long mostRecentMessageTime;
	public String content;
	public String subject;
	public UserInfo mostRecentSender;
	// synchronized content -- ends

	public long messagesUnread;
	public long messageCount;
	public int numOfParticipants;

	public ConversationStatus status;
	public String types;

	public ConversationSummaryBasicInfo(String id, VedantuRecordState recordState) {

		super(id, recordState);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addImageSrcUrl() {

		content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

	}

}
