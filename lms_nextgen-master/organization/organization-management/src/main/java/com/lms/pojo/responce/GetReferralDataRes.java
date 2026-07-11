package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetReferralDataRes {
    public String message;
    public int    friendRewards;
    public int    referrerRewards;
    public int    existingRewardPoints;
    public String referralCode;
}
