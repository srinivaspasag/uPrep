package com.vedantu.organization.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "campaigncodes", noClassnameStored = true)
public class CampaignCode extends VedantuBaseMongoModel {

    public String       salesCampaignId;
    public String       code;
    public boolean      expired;
    public int          maxUsageCount;
    public int          currentUsageCount;
    public List<String> consumerUserIds = new ArrayList<String>();

    public CampaignCode() {
        super();
    }

    public CampaignCode(String salesCampaignId, int maxUsageCount) {
        super();
        this.salesCampaignId = salesCampaignId;
        this.maxUsageCount = maxUsageCount;
    }

}
