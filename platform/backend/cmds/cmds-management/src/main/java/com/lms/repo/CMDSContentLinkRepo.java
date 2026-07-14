package com.lms.repo;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.CMDSContentLink;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CMDSContentLinkRepo extends MongoRepository<CMDSContentLink, String> {
    // List<CMDSContentLink> findAllByIdAndState(List<String> details, VedantuRecordState active);

    List<CMDSContentLink> findAllByIdAndRecordState(List<String> details, VedantuRecordState active);
}
