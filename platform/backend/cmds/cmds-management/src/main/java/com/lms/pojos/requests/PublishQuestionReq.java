package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class PublishQuestionReq extends AbstractAuthCheckReq {
    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "entities should not be null")
    public List<SrcEntity> entities;

}
