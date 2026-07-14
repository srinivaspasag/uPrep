package com.lms.pojo.responce.campaigns;

import com.lms.models.Campaign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCampaignRes {
    public Campaign campaign;

    public AddCampaignRes(Campaign campaign) {
        this.campaign = campaign;
    }

}
