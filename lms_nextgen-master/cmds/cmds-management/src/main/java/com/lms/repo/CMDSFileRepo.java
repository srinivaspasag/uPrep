package com.lms.repo;

import com.lms.models.CMDSFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSFileRepo extends MongoRepository<CMDSFile, String> {

}
