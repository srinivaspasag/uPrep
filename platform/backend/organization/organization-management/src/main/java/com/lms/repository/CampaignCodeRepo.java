package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.CampaignCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignCodeRepo extends MongoRepository<CampaignCode,String> {
    List<CampaignCode> findByCode(String campaignCode);

    CampaignCode findByCodeAndRecordStateAndExpired(String code, VedantuRecordState active);
    CampaignCode findByCodeAndRecordState(String code, VedantuRecordState active);
}
