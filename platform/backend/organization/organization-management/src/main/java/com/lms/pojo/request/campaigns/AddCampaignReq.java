package com.lms.pojo.request.campaigns;

import com.lms.enums.CampaignType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AddCampaignReq {
    public String message;
    public int referrerRewards;
    public int friendRewards;
    public CampaignType campaignType;

    public AddCampaignReq() {
        super();
    }

    public AddCampaignReq(String message, int referrerRewards, int friendRewards,
                          CampaignType campaignType) {
        super();
        this.message = message;
        this.referrerRewards = referrerRewards;
        this.friendRewards = friendRewards;
        this.campaignType = campaignType;
    }

}
