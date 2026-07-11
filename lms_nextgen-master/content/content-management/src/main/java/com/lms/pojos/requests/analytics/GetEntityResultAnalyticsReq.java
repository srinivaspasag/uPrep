package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetEntityResultAnalyticsReq extends AbstractOrgListReq {
    @NotNull(message = "entity Should not be empty")
    public SrcEntity entity;
    public boolean isDetailedResultSheet;
    public double maxScore;
    public double minScore;
    public String studentUserId;
    public String queryText;
}
