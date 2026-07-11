package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.UserEntityAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEntityAttemptRepo extends MongoRepository<UserEntityAttempt,String> {
    UserEntityAttempt findByUserIdAndEntityTypeAndEntityId(String userId, EntityType entityType, String entityId);

    List<UserEntityAttempt> findByIdInAndUserId(Object[] toArray, String userId);
}
