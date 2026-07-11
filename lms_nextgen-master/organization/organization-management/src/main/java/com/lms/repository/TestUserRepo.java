package com.lms.repository;

import com.lms.models.TestUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestUserRepo extends MongoRepository<TestUser, String> {
    TestUser findByMemberId(String memberId);

    List<TestUser> findByOrgIdAndTimeCreatedGreaterThanEqualAndRecordStateIs(String orgId, Long startdate, Enum recordState);
}
