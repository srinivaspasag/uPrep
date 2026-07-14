package com.lms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.models.analytics.UserEntityRatings;

@Repository
public interface UserEntityRatingsRepo extends MongoRepository<UserEntityRatings, String>{

	UserEntityRatings findByUserIdAndSrcEntityAndContentSrc(String userId, SrcEntity entity, SrcEntity contentSrc);

}
