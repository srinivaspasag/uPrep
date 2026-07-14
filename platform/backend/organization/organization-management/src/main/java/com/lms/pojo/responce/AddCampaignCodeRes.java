package com.lms.pojo.responce;

import com.lms.models.CampaignCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCampaignCodeRes {
    public boolean      done;
    public CampaignCode campaignCode;
}
