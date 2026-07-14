package com.lms.repository;

import com.lms.models.OrgMicrositeConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgMicrositeConfigRepo extends MongoRepository<OrgMicrositeConfig,String>{
    OrgMicrositeConfig findByOrgId(String orgId);
}
