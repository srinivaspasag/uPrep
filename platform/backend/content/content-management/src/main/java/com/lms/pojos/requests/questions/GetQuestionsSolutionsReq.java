package com.lms.pojos.requests.questions;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Setter
public class GetQuestionsSolutionsReq extends AbstractOrgListReq {

    @NotNull
    public List<String> qIds;
    public boolean verifiedOnly;
}
