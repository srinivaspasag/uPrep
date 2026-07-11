package com.lms.pojo.responce;

import com.lms.models.CampaignCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CreateBulkCampaignCodeRes {
    public boolean            success;
    public List<CampaignCode> campaignCodes = new ArrayList<CampaignCode>();
}
