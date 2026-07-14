package com.lms.repo;

import com.lms.models.SDCardGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SDCardGroupRepo extends MongoRepository<SDCardGroup, String> {
}
