package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateBulkCampaignCodeReq {
    public int    numberOfCampaignCodesRequired;
    public String salesCampaignId;
}
