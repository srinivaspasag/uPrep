package com.lms.services;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.GetMessageReq;
import com.lms.pojos.requests.GetConversationReq;
import com.lms.pojos.requests.GetUserMailBoxInfoReq;
import com.lms.pojos.requests.UpdateUserMailBoxInfoReq;
import com.lms.pojos.requests.messages.*;
import com.lms.requests.GetConversationUsersReq;
import com.lms.requests.GetMessageSummariesReq;

public interface MessageCenterService {

    VedantuResponse getConversationSummary(GetConversationSummaryReq getMessageSummaryReq);

    VedantuResponse getConversationSummaries(GetConversationSummariesReq getConversationSummariesReq);

    VedantuResponse getMessageSummary(GetMessageSummaryReq getMessageSummaryReq);

    VedantuResponse getMessageSummaries(GetMessageSummariesReq getMessageSummaryReq);

    VedantuResponse sendMessage(SendMessageReq sendMessageReq);

    VedantuResponse markConversation(MarkConversationReq markConversationReq);

    VedantuResponse deleteConversation(DeleteConversationReq deleteConversationReq);

    VedantuResponse getMessage(GetMessageReq getMessageSummaryReq);

    VedantuResponse getUserMailBoxInfo(GetUserMailBoxInfoReq getUserMailBoxInfoReq);


    VedantuResponse updateMailBoxInfo(UpdateUserMailBoxInfoReq updateUserMailBoxInfoReq);

    VedantuResponse getConversationUsers(GetConversationUsersReq getConversationUsersReq);

    VedantuResponse getConversation(GetConversationReq getConversationReq);
}
