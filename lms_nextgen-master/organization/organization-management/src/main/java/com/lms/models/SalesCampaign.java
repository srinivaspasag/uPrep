package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.RewardType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "salescampaigns")
@Setter
@Getter
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

