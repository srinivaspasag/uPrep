package com.lms.repository;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.RankType;
import com.lms.models.ChallengeUserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeUserInfoRepo extends MongoRepository<ChallengeUserInfo, String> {
    ChallengeUserInfo findByUserIdAndTypeAndParentId(String userId, RankType rankType, SrcEntity parent);

    ChallengeUserInfo findByUserIdAndParentIdAndParentTypeAndRankIdentifier(String userId, String id, EntityType type, String identifier);
}
