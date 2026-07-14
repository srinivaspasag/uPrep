package com.lms.repo;

import com.lms.models.CMDSVideo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSVideoRepo extends MongoRepository<CMDSVideo, String> {
}
