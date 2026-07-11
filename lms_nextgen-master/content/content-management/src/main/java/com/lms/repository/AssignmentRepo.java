package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.tests.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepo extends MongoRepository<Assignment, String> {

    Assignment findByCmdsId(String id);

    Assignment findByIdAndRecordState(String id, VedantuRecordState active);

    List<Assignment> findByIdInAndContentType(Object[] toArray, EntityType type);
}
