package com.lms.pojo.request;

import com.lms.enums.RewardType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateSalesCampaignReq {
    @NotBlank(message="salesCampaignId should not be null")
    public String     salesCampaignId;
    public String     name;
    public RewardType rewardType;
    public int        rewardValue;
    public long       startTime;
    public long       expiryTime;
    public boolean    isActive;
}
