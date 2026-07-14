package com.vedantu.content.pojos.requests.questions;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetQuestionsSolutionsReq extends AbstractOrgListReq {

    @Required
    public List<String> qIds;
    public boolean      verifiedOnly;
}
