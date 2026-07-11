package com.lms.pojo.request;

import com.lms.enums.RewardType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddSalesCampaignReq {
    public String     name;
    public RewardType rewardType;
    public int        rewardValue;
    public long       startTime;
    public long       expiryTime;
    public boolean    isActive;
}
