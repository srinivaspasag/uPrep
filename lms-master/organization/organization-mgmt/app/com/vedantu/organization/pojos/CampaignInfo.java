package com.vedantu.organization.pojos;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.organization.enums.CampaignType;

public class CampaignInfo extends ModelBasicInfo {
    public String       message;
    public int          friendRewards;
    public int          referrerRewards;
    public CampaignType campaignType;

    public CampaignInfo(String message, int referrerRewards, int friendRewards,
            CampaignType campaignType) {
        super();
        this.message = message;
        this.referrerRewards = referrerRewards;
        this.friendRewards = friendRewards;
        this.campaignType = campaignType;
    }

}
