package com.vedantu.comm.models.hbase.messages;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.annotate.JsonManagedReference;

import com.vedantu.comm.enums.MessageAction;
import com.vedantu.comm.managers.MessageManager;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.hbase.AbstractHbaseModels;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;

public class MessageV2 extends AbstractHbaseModels implements IReverseImageMapperProcessor {

    public MessageAction   action;
    public String          content;
    public String          conversationId;
    public String          messageId;
    public String          parentMessageId;

    @JsonManagedReference
    public List<SrcEntity> receivers;

    @JsonManagedReference
    public SrcEntity       sender;
    public long            sentTime;
    public String          subject;
    public String          type;
    public String          orgId;

    public MessageV2() {

        // super("messages", "data");
        // // TODO Auto-generated constructor stub
        // wrapper = new
        // HbaseTableWrapper<MessageSummary>(Play.configuration.getProperty(MESSAGES_TABLE),
        // Message.class);
        //
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

    public String getConversationId() {

        return this.conversationId;
    }

    public String getMessageId() {

        return this.messageId;
    }

    public String getParentMessageId() {

        return this.parentMessageId;
    }

    public SrcEntity getSender() {

        return this.sender;
    }

    public long getSentOnTimestamp() {

        return this.sentTime;
    }

    public String getSubject() {

        return this.subject;
    }

    public String getType() {

        return this.type;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public void setConversationId(String conversationId) {

        this.conversationId = conversationId;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public void setParentMessageId(String parentMessageId) {

        this.parentMessageId = parentMessageId;
    }

    public void setSender(SrcEntity sender) {

        this.sender = sender;
    }

    public void setSentOnTimestamp(long sentOnTimestamp) {

        this.sentTime = sentOnTimestamp;
        this.timestamp = sentOnTimestamp;
    }

    public void setSubject(String subject) {

        this.subject = subject;
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

        // TODO Auto-generated method stub
        return messageId;
    }

    public String validate() {

        if (receivers != null) {
            for (SrcEntity receiver : receivers) {
                String value = receiver.validate();
                if (value != null) {
                    return value;
                }
            }
        }
        return null;

    }

    @Override
    public void addImageSrcUrl() {

        content = ImageHTMLUtils.addImageSrcUrl(EntityType.MESSAGE, content);
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

        content = MessageManager.removeTempImageSrcAndSaveToFS(EntityType.MESSAGE, content,
                moveImages,"comm");

    }

}
