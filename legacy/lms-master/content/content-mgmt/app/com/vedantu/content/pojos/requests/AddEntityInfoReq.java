package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.UserRatingType;

public class AddEntityInfoReq extends GetEntityInfoForAppReq {

    @Required
    public UserRatingType rating;
    public String feedback;

}
