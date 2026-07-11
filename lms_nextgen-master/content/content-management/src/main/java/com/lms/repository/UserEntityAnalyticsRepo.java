package com.lms.repository;

import com.lms.models.analytics.UserEntityAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEntityAnalyticsRepo extends MongoRepository<UserEntityAnalytics, String> {

    List<UserEntityAnalytics> findByEntityIdAndAcadDimId(String id, String acadDimId);

    List<UserEntityAnalytics> findByOrgIdAndEntityIdAndAcadDimId(String orgId, String id, String acadDimId);
}
