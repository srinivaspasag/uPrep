package com.lms.repository;

import com.lms.models.analytics.UserQuestionAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuestionAnalyticsRepo extends MongoRepository<UserQuestionAnalytics, String> {

}
