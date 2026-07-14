package com.vedantu.organization.pojos.responses.campaigns;

import com.vedantu.organization.models.Campaign;

public class AddCampaignRes {
    public Campaign campaign;

    public AddCampaignRes(Campaign campaign) {
        this.campaign = campaign;
    }
}
