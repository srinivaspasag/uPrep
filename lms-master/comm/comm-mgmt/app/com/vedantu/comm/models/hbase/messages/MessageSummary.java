package com.vedantu.comm.models.hbase.messages;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonManagedReference;

import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.hbase.AbstractHbaseModels;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;

public class MessageSummary extends AbstractHbaseModels implements IListResponseObj,
        IReverseImageMapperProcessor {

    public static final int   CHARACTER_LIMIT = 200; ;
    public String             userId;
    public String             messageId;
    public String             conversationId;
    public String             userMessageId;
    public String             parentMessageId;
    public String             content;
    public ConversationStatus status;
    public String             types;
    public String             orgId;
    @JsonManagedReference
    public SrcEntity          sender;
    @JsonManagedReference
    public SrcEntity          receiver;

    long                      sentTime        = 0;  // TODO set
    long                      receivedTime    = 0;  // TODO set

    public MessageSummary(String messageId) {

        // super("messages", "data");
        // wrapper = new HbaseTableWrapper<MessageSummary>(
        // Play.configuration.getProperty(MessageUtil.USER_MESSAGE_TABLE),
        // MessageSummary.class);
        this.messageId = messageId;
    }

    public String getParentMessageId() {

        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {

        this.parentMessageId = parentMessageId;
    }

    public long getSentTime() {

        return sentTime;
    }

    public void setSentTime(long sentTime) {

        this.sentTime = sentTime;
    }

    public long getReceivedTime() {

        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {

        this.receivedTime = receivedTime;
    }

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public SrcEntity getSender() {

        return sender;
    }

    public void setSender(SrcEntity sender) {

        this.sender = sender;
    }

    public SrcEntity getReceiver() {

        return receiver;
    }

    public void setReceiver(SrcEntity receiver) {

        this.receiver = receiver;
        if (receiver.type == EntityType.USER) {
            this.userId = receiver.id;
        }
    }

    public String getMessageId() {

        return this.messageId;
    }

    public ConversationStatus getStatus() {

        return this.status;
    }

    public String getTypes() {

        return this.types;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public void setStatus(ConversationStatus status) {

        this.status = status;
    }

    public void setTypes(String types) {

        this.types = types;
    }

    public String getUserMessageId() {

        return userMessageId;
    }

    public void setUserMessageId(String userMessageId) {

        this.userMessageId = userMessageId;
    }

    public String getConversationId() {

        return conversationId;
    }

    public void setConversationId(String conversationId) {

        this.conversationId = conversationId;
    }

    @Override
    public String getKey() {

        return userMessageId;
    }

    public String getNewKey() throws VedantuException {

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(conversationId) || sentTime == 0
                || StringUtils.isEmpty(messageId)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(userId).append("_").append(conversationId).append("_")
                .append(String.format("%020d", Long.MAX_VALUE - sentTime)).append("_")
                .append(messageId);
        userMessageId = builder.toString();
        return userMessageId;
    }

    @Override
    public void addImageSrcUrl() {

        content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

    }

    @Override
    public String toString() {

        return "MessageSummary [userId=" + userId + ", messageId=" + messageId
                + ", conversationId=" + conversationId + ", userMessageId=" + userMessageId
                + ", parentMessageId=" + parentMessageId + ", content=" + content + ", status="
                + status + ", types=" + types + ", orgId=" + orgId + ", sender=" + sender
                + ", receiver=" + receiver + ", sentTime=" + sentTime + ", receivedTime="
                + receivedTime + "]";
    }

}
