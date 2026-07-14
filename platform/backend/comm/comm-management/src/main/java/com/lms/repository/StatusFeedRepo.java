package com.lms.repository;

import com.lms.models.StatusFeed;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusFeedRepo extends MongoRepository<StatusFeed, String> {

}
