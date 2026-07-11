package com.vedantu.cmds.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;

public class GetResourcesReq extends AbstractGetResourcesReq {

    public Difficulty diffculty;

    @Required
    public String     folderId; // directoryId

    public String     quesType = StringUtils.EMPTY;
    public String     paraId = StringUtils.EMPTY;

}
