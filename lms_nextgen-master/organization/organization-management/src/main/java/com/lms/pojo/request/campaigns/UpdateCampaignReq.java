package com.lms.pojo.request.campaigns;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCampaignReq {
    public String id;
    public String message;
    public int referrerRewards;
    public int friendRewards;
}
