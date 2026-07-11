package com.vedantu.comm.requests.messages;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetConversationSummariesReq extends AbstractAuthCheckReq {

    public String  orgId;
    public int     start;
    public int     size;
    public boolean future= false;
    public long    timestamp = -1L;
    public String  conversationId;

}
