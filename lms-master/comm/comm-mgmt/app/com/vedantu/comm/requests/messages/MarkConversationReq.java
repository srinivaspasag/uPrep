package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class MarkConversationReq extends AbstractAuthCheckReq {

    @Required
    public String             userConversationId;
    @Required
    public ConversationStatus status;
    public String             orgId;
}
