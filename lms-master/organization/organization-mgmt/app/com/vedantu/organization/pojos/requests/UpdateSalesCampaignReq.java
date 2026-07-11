package com.vedantu.organization.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.enums.RewardType;

public class UpdateSalesCampaignReq {

    @Required
    public String     salesCampaignId;
    public String     name;
    public RewardType rewardType;
    public int        rewardValue;
    public long       startTime;
    public long       expiryTime;
    public boolean    isActive;

}
