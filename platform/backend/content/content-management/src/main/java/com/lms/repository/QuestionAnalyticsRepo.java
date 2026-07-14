package com.lms.repository;

import com.lms.models.analytics.QuestionAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnalyticsRepo extends MongoRepository<QuestionAnalytics, String> {
}
