package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.CampaignType;
import com.lms.models.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepo extends MongoRepository<Campaign, String> {
    Campaign findByCampaignType(CampaignType campaignType);

    Campaign findByCampaignTypeAndRecordState(CampaignType campaignType, VedantuRecordState vedantuRecordState);

    Campaign findByRecordState(VedantuRecordState active);
}
