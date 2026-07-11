package com.lms.models;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionRepo extends MongoRepository<Discussion, String> {
    Discussion findByUserIdAndContentType(String userId, EntityType type);

    Discussion findByIdAndRecordState(String id, VedantuRecordState active);

    Discussion findByUserIdAndContent(String userId, EntityType type);

    Discussion findByUserId(String userId, EntityType type);
}
