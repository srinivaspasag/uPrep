package com.lms.repository;

import com.lms.models.NewsNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsNotificationRepo extends MongoRepository<NewsNotification, String> {
}
