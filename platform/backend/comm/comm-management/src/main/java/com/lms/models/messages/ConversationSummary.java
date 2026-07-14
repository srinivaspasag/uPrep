package com.lms.models.messages;

import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.ConversationStatus;
import com.lms.interfaces.IReverseImageMapperProcessor;

import java.io.IOException;
import java.util.Comparator;

public class ConversationSummary extends AbstractHbaseModels implements IListResponseObj, IReverseImageMapperProcessor {

	public String conversationId;
	public String userConversationId;
	public String content;
	public String subject;
	public long messageCount;
	// equal to most recent message sent timing
	public long mostRecentMessageTiming;
	public String firstMessageId;
	public SrcEntity mostRecentSender;
	public long messagesUnread;
	public int numOfParticipants;
	public String orgId;
	ConversationStatus status;
	String types;

	public ConversationSummary() {

		// super("conversations", "data");
		messagesUnread = 0;
		messageCount = 0;
		status = ConversationStatus.READ;
		// wrapper = new
		// HbaseTableWrapper<ConversationSummary>(Play.configuration.getProperty(USER_CONVERSATION_TABLE),
		// ConversationSummary.class);
	}

	public int getNumOfParticipants() {

		return numOfParticipants;
	}

	public void setNumOfParticipants(int numOfParticipants) {

		this.numOfParticipants = numOfParticipants;
	}

	public long getMostRecentMessageTiming() {

		return mostRecentMessageTiming;
	}

	public void setMostRecentMessageTiming(long mostRecentMessageTiming) {

		this.timestamp = mostRecentMessageTiming;
		this.mostRecentMessageTiming = mostRecentMessageTiming;
	}

	public String getSubject() {

		return subject;
	}

	public void setSubject(String subject) {

		this.subject = subject;
	}

	public String getContent() {

		return content;
	}

	public void setContent(String content) {

		this.content = content;
	}

	public String getFirstMessageId() {

		return firstMessageId;
	}

	public void setFirstMessageId(String firstMessageId) {

		this.firstMessageId = firstMessageId;
	}

	public SrcEntity getMostRecentSender() {

		return mostRecentSender;
	}

	public void setMostRecentSender(SrcEntity mostRecentSender) {

		this.mostRecentSender = mostRecentSender;
	}

	public long getMessageCount() {

		return messageCount;
	}

	public void setMessageCount(long messageCount) {

		this.messageCount = messageCount;
	}

	public void incrementMessageCount() {

		incrementMessageUnread();
		this.messageCount++;
	}

	public String getConversationId() {

		return this.conversationId;
	}

	public void setConversationId(String conversationId) {

		this.conversationId = conversationId;
	}

	public ConversationStatus getStatus() {

		return this.status;
	}

	// use carefully
	public void setStatus(ConversationStatus status) {

		this.status = status;
	}

	public String getTypes() {

		return this.types;
	}

	public void setTypes(String types) {

		this.types = types;
	}

	public String getUserConversationId() {

		return userConversationId;
	}

	public void setUserConversationId(String userConversationId) {

		this.userConversationId = userConversationId;
	}

	public void incrementMessageUnread() {

		this.messagesUnread++;
		this.status = ConversationStatus.UNREAD;
	}

	public void decrementMessageUnread() {

		if (this.messagesUnread > 0) {
			this.messagesUnread--;
			if (this.messagesUnread <= 0) {
				this.status = ConversationStatus.READ;
			}
		}
	}

	@Override
	public String getKey() {

		return userConversationId;
	}

	@Override
	public void addImageSrcUrl() {

		content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);
	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

	}

	public static class ConversationSummaryRecentMessageTimeSorter implements Comparator<ConversationSummary> {

		@Override
		public int compare(ConversationSummary o1, ConversationSummary o2) {

			return (o1.getMostRecentMessageTiming() > o2.getMostRecentMessageTiming() ? -1
					: (o1.getMostRecentMessageTiming() == o2.getMostRecentMessageTiming() ? 0 : 1));
		}
	}

}
