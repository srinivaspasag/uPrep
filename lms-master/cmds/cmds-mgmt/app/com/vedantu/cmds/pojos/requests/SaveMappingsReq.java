package com.vedantu.cmds.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.board.pojos.BoardMappings;

public class SaveMappingsReq extends GetSharedQuestionsBasicInfoReq{
    @Required
    public String parentOrgId;
    @Required
    public String sharedToOrgId;
    @Required
    public List<BoardMappings> boardMappings;
}
