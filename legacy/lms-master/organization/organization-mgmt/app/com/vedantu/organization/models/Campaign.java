package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.enums.CampaignType;

@Entity(value = "campaigns", noClassnameStored = true)
public class Campaign extends VedantuBaseMongoModel {

    public String       message;
    public int          referrerRewards;
    public int          friendRewards;
    public CampaignType campaignType;

    @Override
    public String toString() {
        return "Campaign [message=" + message + ", referrerRewards=" + referrerRewards
                + ", friendRewards=" + friendRewards + ", campaignType=" + campaignType + "]";
    }

}
