package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ConversationRes extends VedantuBaseMongoModel {

    public String orgId;
    private String conversationId;
    // in conversation
    private String subject;                // Subject of first message
    private String recentMessageId;
    private String firstMesssageId;
    private List<SrcEntity> participants;
    private long messageCount = 0;

    public ConversationRes() {
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

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

   /* public long getTimestamp() {
        return this.timestamp;
    }*/

    public String getRecentMessageId() {
        return this.recentMessageId;
    }

    public void setRecentMessageId(String recentMessageId) {
        this.recentMessageId = recentMessageId;
    }

    /*public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }*/

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }

    public List<SrcEntity> getParticipants() {
        return this.participants;
    }

    public void setParticipants(List<SrcEntity> participants) {
        if (CollectionUtils.isNotEmpty(participants)) {
            this.participants = participants;
        }
    }

    public void addParticipant(SrcEntity entity) {
        if (CollectionUtils.isNotEmpty(participants)) {
            this.participants.add(entity);
        }
    }

    @Override
    public String toString() {

        return "Conversation [conversationId=" + conversationId + ", subject=" + subject
                + ", recentMessageId=" + recentMessageId + ", firstMesssageId=" + firstMesssageId
                + ", participants=" + participants + ", messageCount=" + messageCount + ", orgId="
                + orgId + ", toString()=" + super.toString() + "]";
    }

}
