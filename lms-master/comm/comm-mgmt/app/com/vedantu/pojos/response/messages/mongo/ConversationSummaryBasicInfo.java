package com.vedantu.pojos.response.messages.mongo;

import java.io.IOException;

import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.UserInfo;

public class ConversationSummaryBasicInfo extends ModelBasicInfo implements IListResponseObj,
        IReverseImageMapperProcessor {

    public String             orgId;

    public UserInfo           userInfo;
    public String             conversationId;
    public String             firstMessageId;

    // synchronized content -- starts
    public long               mostRecentMessageTime;
    public String             content;
    public String             subject;
    public UserInfo           mostRecentSender;
    // synchronized content -- ends

    public long               messagesUnread;
    public long               messageCount;
    public int                numOfParticipants;

    public ConversationStatus status;
    public String             types;

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
