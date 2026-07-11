package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrganizationStatus;
import com.lms.models.OrgMember;
import com.lms.models.Organization;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrganizationRepo extends MongoRepository<Organization, String> {

    Organization findByWebsite(String website);

    Organization findBySlug(String slug);

    Organization findByReferer(String refererHost);

    List<Organization> findALLByStatusAndFullName(OrganizationStatus status, String query);

    List<Organization> findAllByStatus(OrganizationStatus status);

    List<Organization> findAllByFullName(String query);

    Organization findBySlugAndWebsite(String trim, String trim1);

    Organization findByIdAndAdminUserId(String orgid,String userid);

    Organization findByIdAndRecordState(String orgId, VedantuRecordState active);

    List<Organization> findAllByIdIn(List<ObjectId> orgIds);

    List<Organization> findAllBySubscriptionPlanId(String planId);
}
