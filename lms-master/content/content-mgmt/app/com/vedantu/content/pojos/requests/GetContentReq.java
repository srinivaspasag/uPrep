package com.vedantu.content.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

public class GetContentReq {
    @Required
    public String       orgId;
    public String       userId;
    public String       programId;
    public String       parentId;
    public List<String> brdIds;
    public Integer      start;
    public Integer      size;
    public boolean      keepModuleResult;

}
