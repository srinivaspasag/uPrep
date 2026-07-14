package com.vedantu.comm.models.hbase.messages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.hbase.AbstractHbaseModels;
import com.vedantu.commons.pojos.SrcEntity;

public class Conversation extends AbstractHbaseModels {

	private String			conversationId;
	private String			subject;				// Subject of first message
													// in conversation

	private String			recentMessageId;
	private String			firstMesssageId;

	private List<SrcEntity>	participants;
	private long			messageCount	= 0;
	public String			orgId;

	public Conversation() {
		// super("conversations", "data");
		participants = new ArrayList<SrcEntity>();
		// wrapper = new HbaseTableWrapper<Conversation>(
		// Play.configuration.getProperty(CONVERSATION_TABLE),
		// Conversation.class);
		messageCount = 0;
	}

	public String getFirstMesssageId() {
		return firstMesssageId;
	}

	public void setFirstMesssageId(String firstMesssageId) {
		this.firstMesssageId = firstMesssageId;
	}

	public String getConversationId() {
		return this.conversationId;
	}

	public String getRecentMessageId() {
		return this.recentMessageId;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public void setRecentMessageId(String recentMessageId) {
		this.recentMessageId = recentMessageId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getSubject() {
		return subject;
	}

	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public void incrementMessageCount() {
		this.messageCount++;
	}

	public List<SrcEntity> getParticipants() {
		return this.participants;
	}

	public void addParticipant(SrcEntity entity) {
		if (CollectionUtils.isNotEmpty(participants)) {
			this.participants.add(entity);
		}
	}

	public void setParticipants(List<SrcEntity> participants) {
		if (CollectionUtils.isNotEmpty(participants)) {
			this.participants = participants;
		}
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return conversationId;
	}

    @Override
    public String toString() {

        return "Conversation [conversationId=" + conversationId + ", subject=" + subject
                + ", recentMessageId=" + recentMessageId + ", firstMesssageId=" + firstMesssageId
                + ", participants=" + participants + ", messageCount=" + messageCount + ", orgId="
                + orgId + ", toString()=" + super.toString() + "]";
    }

}
