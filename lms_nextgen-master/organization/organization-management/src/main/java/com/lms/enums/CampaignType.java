package com.lms.enums;

public enum CampaignType {
    REFERRAL, SALES, UNKNOWN;

    public static CampaignType valueOfKey(String value) {
        CampaignType campaignType = UNKNOWN;
        try {
            campaignType = CampaignType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return campaignType;
    }
}
