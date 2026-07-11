package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.ChallengeLeaderBoard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeLeaderBoardRepo extends MongoRepository<ChallengeLeaderBoard, String> {
    ChallengeLeaderBoard findByChallengeIdAndParentIdAndParentType(String challengeId, String orgId, EntityType name);
}
