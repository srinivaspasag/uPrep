package com.lms.repository;

import com.lms.models.UserMailBoxInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMailBoxInfoRepo extends MongoRepository<UserMailBoxInfo, String> {

    UserMailBoxInfo findByUserId(String userId);

}
