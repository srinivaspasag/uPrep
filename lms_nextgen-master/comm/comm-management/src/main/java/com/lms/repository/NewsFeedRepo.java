package com.lms.repository;

import com.lms.models.NewsFeed;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsFeedRepo extends MongoRepository<NewsFeed, String> {
    //NewsFeed findByUserIdAndCount(String userId, int size);
}
