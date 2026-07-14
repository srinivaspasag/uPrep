package com.lms.repo;

import com.lms.models.SDCard;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SdcardRepo extends MongoRepository<SDCard, String> {
    List<SDCard> findById(List<ObjectId> ids);

}
