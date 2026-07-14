package com.lms.repo;

import com.lms.models.ContentGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentGroupRepo extends MongoRepository<ContentGroup, String> {
}
