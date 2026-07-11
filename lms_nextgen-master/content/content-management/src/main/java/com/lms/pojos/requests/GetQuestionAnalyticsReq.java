package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.AnswerCorrectness;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetQuestionAnalyticsReq extends AbstractAuthCheckReq {

    @NotBlank(message = "entityId should not be null")
    public String entityId;
    @NotBlank(message = "EntityType should not be null")
    public EntityType entityType;
    @NotBlank(message = "qId should not be null")
    public String qId;
    public String orgId;

    public AnswerCorrectness isCorrect;
    public SrcEntity parentEntity;

}
