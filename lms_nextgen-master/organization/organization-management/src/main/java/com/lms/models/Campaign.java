package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.CampaignType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "campaigns")
public class Campaign extends VedantuBaseMongoModel {

    public String message;
    public int referrerRewards;
    public int friendRewards;
    public CampaignType campaignType;

    @Override
    public String toString() {
        return "Campaign [message=" + message + ", referrerRewards=" + referrerRewards
                + ", friendRewards=" + friendRewards + ", campaignType=" + campaignType + "]";
    }

}
