package com.lms.repo;

import com.lms.models.CMDSModule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSModuleRepo extends MongoRepository<CMDSModule, String> {
}
