package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojos.CMDSQuestionInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetCMDSQuestionRes implements IListResponseObj {
    public CMDSQuestionInfo info;
}
