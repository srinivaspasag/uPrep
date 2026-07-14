package com.lms.repo;

import com.lms.models.CMDSDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CMDSDocumentRepo extends MongoRepository<CMDSDocument, String> {

}
