package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCampaignCodeReq {
    public String salesCampaignId;
    public int    maxUsageCount;
}
