package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Module;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepo extends MongoRepository<Module, String> {

    Optional<Module> findById(String moduleId);

    Module findByCmdsModuleId(String id);

    List<Module> findByIdInAndContentType(Object[] toArray, EntityType type);

	List<VedantuBaseMongoModel> findByIdAndRecordState(String entityId, VedantuRecordState active);
}
