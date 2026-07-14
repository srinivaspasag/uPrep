package com.lms.pojos.response;

import com.lms.models.UserMailBoxInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUserMailBoxInfoRes {

    public String userId;
    public long conversationCount;
    public long unreadConversationCount;
    public long sentCount;

    public GetUserMailBoxInfoRes(UserMailBoxInfo userMailBoxInfo) {
        this.userId = userMailBoxInfo.userId;
        this.conversationCount = userMailBoxInfo.conversationCount;
        this.unreadConversationCount = userMailBoxInfo.unreadConversationCount;
        this.sentCount = userMailBoxInfo.sentCount;
    }
}
