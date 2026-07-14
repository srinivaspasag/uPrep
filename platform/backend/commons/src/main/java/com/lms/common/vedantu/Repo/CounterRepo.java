package com.lms.common.vedantu.Repo;

import com.lms.common.vedantu.model.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepo extends MongoRepository<Counter,String>{


    Counter findByFieldAndCollection(String field, String collectionName);
}
