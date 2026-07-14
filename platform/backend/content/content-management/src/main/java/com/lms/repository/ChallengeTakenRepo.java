package com.lms.repository;

import com.lms.models.ChallengeTaken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeTakenRepo extends MongoRepository<ChallengeTaken, String> {
    ChallengeTaken findByChallengeIdAndUserId(String id, String userId);
}
