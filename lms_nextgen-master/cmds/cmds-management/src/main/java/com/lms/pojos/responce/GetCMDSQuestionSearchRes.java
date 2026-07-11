package com.lms.pojos.responce;


import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojos.CMDSQuestionInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetCMDSQuestionSearchRes extends ListResponse<CMDSQuestionInfo> {

    public long paraHits;
    public long nonParaHits;

}
