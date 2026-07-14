package com.lms.repository;

import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.RevenueModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgSection;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface OrgSectionRepo extends MongoRepository<OrgSection, String> {
    Long countByAccessScopeAndOrgId(AccessScope accessScope, String orgid);

    List<OrgSection> findAllByProgramIdAndRecordStateAndRevenueModelAndAccessScopeAndOrgIdIn(String programId, VedantuRecordState recordState, RevenueModel revenueModel, AccessScope accessScope, List<String> asList);

    List<OrgSection> findAllByIdInAndProgramIdAndRecordStateAndRevenueModelAndAccessScopeAndAndOrgIdIn(List<ObjectId> sectionsIds, String programId, VedantuRecordState recordState, RevenueModel revenueModel, AccessScope accessScope, List<String> asList);

    List<OrgSection> findAllByIdInAndRecordStateAndRevenueModelAndAccessScopeAndOrgIdIn(List<ObjectId> sectionsIds, VedantuRecordState recordState, RevenueModel revenueModel, AccessScope accessScope, List<String> asList);

    List<OrgSection> findAllByIdInAndProgramIdAndRecordStateAndRevenueModelAndOrgIdIn(List<ObjectId> sectionsIds, String programId, VedantuRecordState recordState, RevenueModel revenueModel, List<String> asList);

    List<OrgSection> findAllByIdInAndProgramIdAndRecordStateAndAccessScopeAndOrgIdIn(List<ObjectId> sectionsIds, String programId, VedantuRecordState recordState, AccessScope accessScope, List<String> asList);

    List<OrgSection> findAllByIdInAndProgramIdAndRevenueModelAndAccessScopeAndOrgIdIn(List<ObjectId> sectionsIds, String programId, RevenueModel revenueModel, AccessScope accessScope, List<String> asList);

    OrgSection findByAccessCodeAndRecordState(String accessCode, VedantuRecordState active);

    List<OrgSection> findAllByIdInAndProgramIdAndOrgIdIn(List<ObjectId> sectionsIds, String programId, List<String> asList);

    List<OrgSection> findAllByIdInAndRevenueModelAndAccessScope(List<String> orgSectinIds, RevenueModel paid, AccessScope open);

    List<OrgSection> findByIdInAndRecordState(Set<String> sectionIds, VedantuRecordState active);

    List<OrgSection> findAllByOrgIdAndRecordState(String orgid, VedantuRecordState active);

    List<OrgSection> findAllByProgramId(String programid);

    OrgSection findByIdAndOrgId(String orgId, String toSectionId);

    OrgSection findByOrgIdAndProgramIdAndCenterIdAndCodeOrderByCName(String orgId, String programId, String id,
                                                                     String sectionCode);

    List<OrgSection> findAllByIdInAndRecordStateAndAccessScopeAndOrgIdIn(List<ObjectId> sectionsIds,
                                                                         VedantuRecordState recordState, AccessScope accessScope, List<String> asList);

    OrgSection findByOrgIdAndCode(String orgId, String code);

    List<OrgSection> findAllByIdIn(Set<String> sectionIds);

    List<OrgSection> findByIdIn(Collection<String> ids);

    //List<OrgSection> findAllByIdInRevenueModelAndAccessScope(List<String> orgSectinIds, RevenueModel paid, AccessScope open);
}
