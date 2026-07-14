package com.lms.repository;

import com.lms.models.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenRepo extends MongoRepository<UserToken, String> {

    UserToken findByUserId(String userid);

}
