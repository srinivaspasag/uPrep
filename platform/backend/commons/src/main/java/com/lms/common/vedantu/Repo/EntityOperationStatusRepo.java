package com.lms.common.vedantu.Repo;

import com.lms.common.vedantu.mongo.EntityOperationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityOperationStatusRepo extends MongoRepository<EntityOperationStatus, String> {

	List<EntityOperationStatus> findById(List<String> jobIds);

}
