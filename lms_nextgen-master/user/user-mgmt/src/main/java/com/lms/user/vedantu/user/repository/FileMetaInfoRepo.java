package com.lms.user.vedantu.user.repository;

import com.lms.common.vedantu.mongo.FileMetaInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaInfoRepo extends MongoRepository<FileMetaInfo, String>{
}
