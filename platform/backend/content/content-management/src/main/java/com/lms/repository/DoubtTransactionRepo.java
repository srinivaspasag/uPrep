package com.lms.repository;

import com.lms.models.DoubtTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoubtTransactionRepo extends MongoRepository<DoubtTransaction,String> {
    DoubtTransaction findByDiscussionId(String discussionId);
}
