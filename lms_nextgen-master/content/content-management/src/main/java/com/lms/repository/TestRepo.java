package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.Test;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepo extends MongoRepository<Test, String> {

    Test findByCmdsTestId(String cmdsTestId);

    Test findByIdAndRecordState(String id, String active);

    List<Test> findAllByIdIn(List<String> childrenIds);

    List<Test> findByIdInAndContentType(Object[] toArray, EntityType type);
}
