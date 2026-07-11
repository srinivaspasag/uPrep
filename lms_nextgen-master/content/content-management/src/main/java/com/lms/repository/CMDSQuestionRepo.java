package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.CMDSQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSQuestionRepo extends MongoRepository<CMDSQuestion, String> {

    CMDSQuestion findByIdAndRecordState(String id, VedantuRecordState deleted);
}
