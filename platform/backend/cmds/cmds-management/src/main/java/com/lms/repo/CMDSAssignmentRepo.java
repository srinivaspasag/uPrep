package com.lms.repo;

import com.lms.models.CMDSAssignment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CMDSAssignmentRepo extends MongoRepository<CMDSAssignment, String> {
    // List<CMDSAssignment> findByMetadataQidsIn(String globalQid);

    List<CMDSAssignment> findAllById(List<ObjectId> objectIds);
}
