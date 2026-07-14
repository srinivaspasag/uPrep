package com.lms.models;

import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.ConversationStatus;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.response.messages.ConversationSummaryBasicInfo;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;

@Document(value = "userconversations")
@Setter
@Getter
public class ConversationSummary extends VedantuBaseMongoModel
		implements IListResponseObj, IReverseImageMapperProcessor {

	public String orgId;

	public String userId;
	public String conversationId;
	public String firstMessageId;

	// synchronized content -- starts
	// equal to most recent message sent timing
	public long mostRecentMessageTime;
	public String mostRecentMessageId;
	public String content;
	public String subject;
	public String mostRecentSenderId;
	// synchronized content -- ends

	public int messagesUnread;
	public int messageCount;
	public int numOfParticipants;

	public ConversationStatus status = ConversationStatus.UNREAD;

	@Override
	public void addImageSrcUrl() {

		content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

	}

	@Override
	public ModelBasicInfo toBasicInfo() {

		ConversationSummaryBasicInfo basicInfo = new ConversationSummaryBasicInfo(this._getStringId(),
				this.recordState);
		basicInfo.content = this.content;
		basicInfo.subject = this.subject;
		basicInfo.userInfo = new UserInfo(this.userId, VedantuRecordState.ACTIVE);
		basicInfo.mostRecentSender = new UserInfo(this.mostRecentSenderId, VedantuRecordState.ACTIVE);

		basicInfo.conversationId = this.conversationId;
		basicInfo.messageCount = this.messageCount;
		basicInfo.messagesUnread = this.messagesUnread;
		basicInfo.mostRecentMessageTime = this.mostRecentMessageTime;
		basicInfo.status = this.status;

		basicInfo.orgId = this.orgId;
		basicInfo.numOfParticipants = this.numOfParticipants;

		return basicInfo;

	}

}
