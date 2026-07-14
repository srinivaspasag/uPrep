package com.lms.pojo.request;

import com.lms.enums.CampaignType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetReferralDataReq {

    public String userId;
    public CampaignType campaignType;

}
