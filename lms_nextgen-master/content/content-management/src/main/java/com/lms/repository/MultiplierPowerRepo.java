package com.lms.repository;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.MultiplierPower;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultiplierPowerRepo extends MongoRepository<MultiplierPower, String> {

    List<MultiplierPower> findByUserIdAndTypeAndParentIdAndParentType(String userId, String name, String id, EntityType type);
}
