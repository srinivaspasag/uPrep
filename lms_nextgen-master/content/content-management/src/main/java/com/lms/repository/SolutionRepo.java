package com.lms.repository;

import com.lms.models.Solution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SolutionRepo extends MongoRepository<Solution,String>
{
}
