package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;

public class DeleteMappingReq extends GetSharedQuestionsBasicInfoReq{
    @Required
    public String parentOrgId;
    @Required
    public String sharedToOrgId;
    @Required
    public String parentBoardId;
    @Required
    public String sharedToBoardId;

    public boolean visible;

    public boolean reSync;
    // This boolean is used when adding new para question to text question. This is not used while sharing questions
    public boolean addNewPara;
}
