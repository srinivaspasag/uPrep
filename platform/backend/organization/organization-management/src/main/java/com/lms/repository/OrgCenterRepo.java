package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrgCenterRepo extends MongoRepository<OrgCenter, String> {
    List<OrgCenter> findByOrgIdOrderByCNameAsc(String orgId);

    OrgCenter findByOrgIdAndCodeOrderByCNameAsc(String orgId, String code);

    List<OrgCenter> findByIdIn(List<String> centerIds);

    List<OrgCenter> findByIdInAndRecordState(Set<String> centerIds, VedantuRecordState active);

	List<OrgCenter> findByOrgIdAndCodeInOrderByCName(String orgId, Set<String> codes);


    List<OrgCenter> findAllByIdIn(Set<String> centerIds);
}
