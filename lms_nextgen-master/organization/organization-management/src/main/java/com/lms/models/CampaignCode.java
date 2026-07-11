package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "campaigncodes")
@Setter
@Getter
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

