package com.lms.repository;

import com.lms.models.Channel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelsRepo extends MongoRepository<Channel, String> {
        List<Channel> findByContentSrcTypeAndContentSrcId(String type, String orgId);
}
