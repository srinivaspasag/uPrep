package com.lms.repository;

import com.lms.models.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends MongoRepository<Category, String> {

    List<Category> findByOrgIdOrderByPriorityAsc(String orgId);

    Category findByOrgIdAndName(String orgId, String name);

    List<Category> findByPriority(int priority);

}
