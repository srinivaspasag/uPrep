package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

public class GetEntityReviewsReq extends GetEntityInfoForAppReq{
    @Required
    public int size;
    @Required
    public int start;

    public String ratingType;
}
