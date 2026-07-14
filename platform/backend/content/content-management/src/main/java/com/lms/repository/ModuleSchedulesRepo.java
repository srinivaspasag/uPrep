package com.lms.repository;

import com.lms.models.ModuleSchedules;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleSchedulesRepo extends MongoRepository<ModuleSchedules, String> {
}
