package com.lms.repository;

import com.lms.models.EntityShare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityShareRepo extends MongoRepository<EntityShare, String> {

}
