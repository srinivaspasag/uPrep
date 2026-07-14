package com.lms.pojo.request.campaigns;

import com.lms.enums.CampaignType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetCampaignsReq {
    public CampaignType campaignType;

}
