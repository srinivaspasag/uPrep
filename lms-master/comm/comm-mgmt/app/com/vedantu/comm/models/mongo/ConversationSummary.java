package com.vedantu.comm.models.mongo;

import java.io.IOException;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.pojos.response.messages.mongo.ConversationSummaryBasicInfo;
import com.vedantu.user.pojos.UserInfo;

@Entity(value = "userconversations", noClassnameStored = true)
public class ConversationSummary extends VedantuBaseMongoModel implements IListResponseObj,
        IReverseImageMapperProcessor {

    public String             orgId;

    public String             userId;
    public String             conversationId;
    public String             firstMessageId;

    // synchronized content -- starts
    // equal to most recent message sent timing
    public long               mostRecentMessageTime;
    public String             mostRecentMessageId;
    public String             content;
    public String             subject;
    public String             mostRecentSenderId;
    // synchronized content -- ends

    public int                messagesUnread;
    public int                messageCount;
    public int                numOfParticipants;

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

        ConversationSummaryBasicInfo basicInfo = new ConversationSummaryBasicInfo(
                this._getStringId(), this.recordState);
        basicInfo.content = this.content;
        basicInfo.subject = this.subject;
        basicInfo.userInfo = new UserInfo(this.userId, VedantuRecordState.ACTIVE);
        basicInfo.mostRecentSender = new UserInfo(this.mostRecentSenderId,
                VedantuRecordState.ACTIVE);

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
