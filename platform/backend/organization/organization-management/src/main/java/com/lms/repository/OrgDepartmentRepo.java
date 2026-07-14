package com.lms.repository;

import com.lms.models.OrgDepartment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgDepartmentRepo extends MongoRepository<OrgDepartment, String> {
    List<OrgDepartment> findAllByOrgId(String orgId);

    OrgDepartment findByOrgIdAndCode(String orgId, String code);
}
