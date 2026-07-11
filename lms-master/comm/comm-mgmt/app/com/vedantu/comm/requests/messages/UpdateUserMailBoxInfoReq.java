package com.vedantu.comm.requests.messages;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;


public class UpdateUserMailBoxInfoReq extends AbstractAuthCheckReq {
    public List<String> userIds;
}
