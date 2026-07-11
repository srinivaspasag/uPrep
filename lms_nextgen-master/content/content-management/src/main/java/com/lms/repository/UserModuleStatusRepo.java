package com.lms.repository;


import com.lms.models.UserModuleStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserModuleStatusRepo extends MongoRepository<UserModuleStatus, String> {
    UserModuleStatus findByUserIdAndModuleId(String userid, String moduleid);
}
