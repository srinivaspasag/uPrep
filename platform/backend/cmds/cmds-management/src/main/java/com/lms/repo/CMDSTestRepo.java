package com.lms.repo;

import com.lms.models.CMDSTest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSTestRepo extends MongoRepository<CMDSTest, String> {


    // List<CMDSTest> findByMetaDataDetailsQidsIn(String globalQid);
}
