package com.vedantu.cmds.pojos.requests.notifications;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetRegIdsReq extends AbstractOrgListReq {
    public List<String> programNames;
}