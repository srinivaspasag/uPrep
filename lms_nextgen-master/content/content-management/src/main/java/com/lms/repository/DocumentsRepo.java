package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.Documents;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentsRepo extends MongoRepository<Documents, String> {

    Documents findByCmdsDocId(String id);

    List<Documents> findByIdInAndContentType(Object[] toArray, EntityType type);
}
