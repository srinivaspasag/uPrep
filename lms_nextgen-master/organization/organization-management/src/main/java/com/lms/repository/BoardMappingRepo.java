package com.lms.repository;

import com.lms.models.BoardMapping;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMappingRepo extends MongoRepository<BoardMapping, ObjectId> {
    Optional<BoardMapping> findBySharedToOrgId(String orgId);

    List<BoardMapping> findByparentOrgId(String orgId);
}
