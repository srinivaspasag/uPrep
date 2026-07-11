package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgProgram;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrgProgramRepo extends MongoRepository<OrgProgram, String> {

	List<OrgProgram> findByOrgIdOrDepartmentIdOrderByCName(String orgId, String departmentId);

	OrgProgram findByOrgIdAndDepartmentIdAndCodeOrderByCName(String orgId, String departmentId, String code);

    List<OrgProgram> findByIdInAndRecordState(Set<String> programIds, VedantuRecordState active);

    List<OrgProgram> findByOrgIdInAndCourseIdsOrderByCName(List<String> orgIds, String courseId);


    List<OrgProgram> findAllByOrgIdAndDepartmentId(String id, String orgId);

    List<OrgProgram> findAllByOrgId(String orgId);

    List<OrgProgram> findAllByIdIn(Set<String> programIds);

    OrgProgram findByOrgIdAndId(String orgid, String programId);

    OrgProgram findByIdAndOrgIdIn(String programId,List<String> orgIds);
}
