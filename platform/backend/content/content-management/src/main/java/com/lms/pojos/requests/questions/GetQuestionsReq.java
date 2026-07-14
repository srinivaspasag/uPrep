package com.lms.pojos.requests.questions;

import com.lms.pojos.requests.AbstractContentSearchReq;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetQuestionsReq extends AbstractContentSearchReq {

    public String quesType;
    public String paraId;

}
