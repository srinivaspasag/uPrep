package com.lms.repository;

import com.lms.models.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepo extends MongoRepository<Challenge, String> {
}
