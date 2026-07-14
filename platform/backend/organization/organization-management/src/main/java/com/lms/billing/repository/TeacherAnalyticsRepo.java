package com.lms.billing.repository;


import com.lms.billing.model.TeacherAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherAnalyticsRepo extends MongoRepository<TeacherAnalytics, String> {
    TeacherAnalytics findByTeacherOrgMemberId(String teacherId);
}
