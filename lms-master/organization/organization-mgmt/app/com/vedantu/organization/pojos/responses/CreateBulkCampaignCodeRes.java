package com.vedantu.organization.pojos.responses;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.organization.models.CampaignCode;

public class CreateBulkCampaignCodeRes {

    public boolean            success;
    public List<CampaignCode> campaignCodes = new ArrayList<CampaignCode>();

}
