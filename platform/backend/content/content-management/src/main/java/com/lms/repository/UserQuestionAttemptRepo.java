package com.lms.repository;

import com.lms.models.analytics.UserQuestionAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuestionAttemptRepo extends MongoRepository<UserQuestionAttempt, String> {
    List<UserQuestionAttempt> findByAttemptId(String id);
}
