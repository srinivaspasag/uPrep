package com.vedantu.cmds.pojos.responses.questions;

import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.commons.pojos.responses.ListResponse;

public class GetCMDSQuestionSearchRes extends ListResponse<CMDSQuestionInfo> {

    public long paraHits;
    public long nonParaHits;

}
