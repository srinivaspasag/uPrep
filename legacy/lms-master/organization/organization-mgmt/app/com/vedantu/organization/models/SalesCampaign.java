package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.enums.RewardType;

@Entity(value = "salescampaigns", noClassnameStored = true)
public class SalesCampaign extends VedantuBaseMongoModel {

    public String     name;
    public RewardType rewardType;
    public int        rewardValue;
    public long       startTime;
    public long       expiryTime;
    public boolean    isActive;

    public SalesCampaign() {
        super();
    }

    public SalesCampaign(String name, RewardType rewardType, int rewardValue, long startTime,
            long expiryTime, boolean isActive) {
        super();
        this.name = name;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
        this.isActive = isActive;
    }
}
