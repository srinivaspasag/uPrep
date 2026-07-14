package com.lms.repository;

import com.lms.models.Remark;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemarkRepo extends MongoRepository<Remark, String> {

}
