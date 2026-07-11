package com.lms.user.vedantu.user.repository;

import com.lms.user.vedantu.user.model.UserSalt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSaltrepo extends MongoRepository<UserSalt, String> {
    UserSalt findByUsername(String username);

    List<UserSalt> findByUsernameIn(List<String> usernames);
}
