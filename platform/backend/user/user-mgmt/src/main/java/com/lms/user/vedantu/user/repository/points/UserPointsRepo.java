package com.lms.user.vedantu.user.repository.points;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lms.user.vedantu.user.model.points.UserPoints;
@Repository
public interface UserPointsRepo extends MongoRepository<UserPoints, String>{

	UserPoints findByUserId(String userId);

}
