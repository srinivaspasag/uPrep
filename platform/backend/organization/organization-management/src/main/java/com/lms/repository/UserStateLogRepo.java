package com.lms.repository;

import com.lms.models.UserStateLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateLogRepo extends MongoRepository<UserStateLog, String> {

}
