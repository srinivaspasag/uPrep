package com.lms.user.vedantu.user.repository;

import com.lms.user.vedantu.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String newUsername);

   // List<User> findAllByUsername(String targetUserId);


    Optional<User> findByUsernameAndPassword(String username, String saltedPassword);

    List<User> findByIdIn(List<String> userIds);

	List<User> findByUsernameIn(Set<String> usernames);


    List<User> findByTimeCreated(long minTimeCreated);
}
