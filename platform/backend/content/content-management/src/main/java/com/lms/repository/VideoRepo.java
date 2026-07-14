package com.lms.repository;

import com.lms.models.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepo extends MongoRepository<Video,String> {

    Video findByCmdsVideoId(String id);
}
