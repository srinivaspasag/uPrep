package com.vedantu.organization.pojos.requests;

import com.vedantu.organization.enums.RewardType;

public class AddSalesCampaignReq {

    public String     name;
    public RewardType rewardType;
    public int        rewardValue;
    public long       startTime;
    public long       expiryTime;
    public boolean    isActive;
}
