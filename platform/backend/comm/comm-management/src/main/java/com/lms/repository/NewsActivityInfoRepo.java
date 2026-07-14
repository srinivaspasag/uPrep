package com.lms.repository;

import com.lms.models.NewsActivityInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsActivityInfoRepo extends MongoRepository<NewsActivityInfo, String> {

}
