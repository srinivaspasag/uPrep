package com.lms.board.repo;

import com.lms.board.model.GranteeOrgProgram;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GranteeOrgProgramRepo extends MongoRepository<GranteeOrgProgram, String> {
    Long countBySubscriberOrgIdAndRecordState(String accessScope, VedantuRecordState recordSate);

    List<GranteeOrgProgram> findALLByProviderOrgIdAndRecordState(String providerOrgId, VedantuRecordState active);

    List<GranteeOrgProgram> findAllByProviderOrgIdAndRecordStateAndProgramId(String providerOrgId, VedantuRecordState active, String programId);

    GranteeOrgProgram findByProviderOrgIdAndSubscriberOrgIdAndProgramId(String providerOrgId, String subscriberOrgId, String programId);

    List<GranteeOrgProgram> findBySubscriberOrgIdAndRecordState(String providerOrgId, VedantuRecordState active);
    //List<GranteeOrgProgram> findAllBySubscriberOrgIdAndRecordStateAndDepartmentId(String providerOrgId, VedantuRecordState active, String departmentId);

    List<GranteeOrgProgram> findAllBySubscriberOrgIdAndRecordState(String providerOrgId, VedantuRecordState active);

    //  List<GranteeOrgProgram> findAllBySubscriberOrgIdAndRecordStateAndDepartmentId(String providerOrgId, VedantuRecordState active);
}
