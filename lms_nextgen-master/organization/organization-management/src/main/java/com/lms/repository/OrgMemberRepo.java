package com.lms.repository;

import com.lms.models.OrgMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrgMemberRepo extends MongoRepository<OrgMember, String> {
    OrgMember findByOrgIdAndMemberId(String orgId, String superAdminMemberId);

    OrgMember findByOrgIdAndUserId(String orgId, String userId);

    List<OrgMember> findByOrgIdAndMappingsProgramId(String subscriberOrgId, String programId);

    List<OrgMember> findAllByUserId(String userId);

    OrgMember findByUserId(String userId);

    OrgMember findByCountryCodeAndContactNumber(String countryCode, String contactNumber);

    List<OrgMember> findAllByCountryCodeAndContactNumber(String countryCode, String contactNumber);

    List<OrgMember> findByOrgIdAndProfile(String orgId, String name);

    List<OrgMember> findByOrgIdAndProfileAndUserId(String orgId, String name, String targetUserId);

    List<OrgMember> findByOrgIdAndProfileAndLastUpdated(String orgId, long lastUpdated);

    List<OrgMember> findByContactNumberAndCountryCode(String contactNumber, String countryCode);

    OrgMember findByIdAndOrgIdAndStatus(String memberId, String orgId, String fieldStatus);

    OrgMember findByIdAndOrgId(String memberId, String orgId);

    OrgMember findByIdAndEmailAndStatus(String orgId, String email, boolean b);

    OrgMember findByReferrerCode(String referralcode);

    OrgMember findByReferralCode(String referralCode);

    OrgMember findByIdAndEmail(String orgId, String email);

    OrgMember findByOrgIdAndEmail(String orgId, String email);

    OrgMember findByOrgIdAndEmailAndStatus(String orgId, String email, boolean b);

	List<OrgMember> findByOrgIdAndMemberIdIn(String orgId, Collection<String> memberIds);
	
	OrgMember findByMemberIdAndOrgId(String memberId, String orgId);
    List<OrgMember> findAllByOrgId(String orgId);

    List<OrgMember> findAllByOrgIdAndCanImpersonate(String orgId, boolean b);

    List<OrgMember> findByOrgIdAndTimeCreatedGreaterThanEqualAndRecordStateIsAndProfileIs(String orgId, Long startdate, Enum recordState, Enum OrgMemberProfile);


    List<OrgMember> findAllByFirstNameStartsWithOrLastNameStartsWithOrMemberIdStartsWith(String firstname, String lastname, String memberid);

    List<OrgMember> findByUserIdAndStatusDeviceIdAndStatusDeviceType(String OrgId,String deviceid,String devicetype);

    OrgMember findByOrgIdAndMemberIdAndStatus(String orgId, String memberId, String fieldStatus);
}
