package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.SalesCampaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesCampaignRepo extends MongoRepository<SalesCampaign,String> {
    List<SalesCampaign> findAllByRecordState(VedantuRecordState active);

    SalesCampaign findByIdAndRecordState(String salesCampaignId,VedantuRecordState stste);


}
