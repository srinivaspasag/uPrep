package com.lms.repo;

import com.lms.models.CMDSQuestionSet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSQuestionSetRepo extends MongoRepository<CMDSQuestionSet, String> {
}
