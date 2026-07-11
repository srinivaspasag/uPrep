package com.lms.repository;

import com.lms.models.analytics.UserAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnalyticsRepo extends MongoRepository<UserAnalytics, String> {


}
