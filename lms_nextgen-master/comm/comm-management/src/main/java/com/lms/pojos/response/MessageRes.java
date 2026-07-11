package com.lms.pojos.response;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.MessageAction;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.models.messages.AbstractHbaseModels;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Setter
@Getter
public class MessageRes extends AbstractHbaseModels implements IReverseImageMapperProcessor {

    public MessageAction action;
    public String content;
    public String conversationId;
    public String messageId;
    public String parentMessageId;

    @JsonManagedReference
    public List<SrcEntity> receivers;

    @JsonManagedReference
    public SrcEntity sender;
    public long sentOnTimestamp;
    public String subject;
    public String type;
    public String orgId;

    public MessageRes() {

    }

    public MessageAction getAction() {

        return action;
    }

    public void setAction(MessageAction action) {

        this.action = action;
    }

    public String getContent() {

        return this.content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public String getConversationId() {

        return this.conversationId;
    }

    public void setConversationId(String conversationId) {

        this.conversationId = conversationId;
    }

    public String getMessageId() {

        return this.messageId;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public String getParentMessageId() {

        return this.parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {

        this.parentMessageId = parentMessageId;
    }

    public SrcEntity getSender() {

        return this.sender;
    }

    public void setSender(SrcEntity sender) {

        this.sender = sender;
    }

    public long getSentOnTimestamp() {

        return this.sentOnTimestamp;
    }

    public void setSentOnTimestamp(long sentOnTimestamp) {

        this.sentOnTimestamp = sentOnTimestamp;
        this.timestamp = sentOnTimestamp;
    }

    public String getSubject() {

        return this.subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getType() {

        return this.type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public List<SrcEntity> getReceivers() {

        return receivers;
    }

    public void setReceivers(List<SrcEntity> receivers) {

        this.receivers = receivers;
    }

    @Override
    public String getKey() {

        return messageId;
    }

    public String validate() {

        if (receivers != null) {
            for (SrcEntity receiver : receivers) {
              /*  String value = receiver.validate();
                if (value != null) {
                    return value;
                }*/
            }
        }
        return null;

    }

    @Override
    public void addImageSrcUrl() {

        content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

       /* content = messa.removeTempImageSrcAndSaveToFS(EntityType.MESSAGE, content,
                moveImages,"comm");*/

    }

    @Override
    public String toString() {

        return "Message [action=" + action + ", content=" + content + ", conversationId="
                + conversationId + ", messageId=" + messageId + ", parentMessageId="
                + parentMessageId + ", receivers=" + receivers + ", sender=" + sender
                + ", sentOnTimestamp=" + sentOnTimestamp + ", subject=" + subject + ", type="
                + type + ", orgId=" + orgId + ", getTimestamp()=" + getTimestamp() + "]";
    }

}
