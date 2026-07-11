package com.vedantu.organization.pojos.requests;

import play.data.validation.Constraints.Required;

public class CustomizeCategoryReq {
    @Required
    public String id;
    public String description;
    public String shortDescription;
    public int    priority;
    public String banner;
    public String thumbnail;
    public String iconUUID;
    public String bannerUUID;
}
