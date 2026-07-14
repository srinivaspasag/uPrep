package com.vedantu.organization.daos;

import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.billing.managers.OrderManager;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.organization.enums.CampaignType;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.Campaign;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.pojos.MemberParentInfo;
import com.vedantu.organization.pojos.OrgMemberExtraInfo;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.enums.UserStatus;
import com.vedantu.user.models.LoginStatus;
import com.vedantu.user.models.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;

import play.Logger;
import play.Logger.ALogger;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class OrgMemberDAO extends VedantuBasicDAO<OrgMember, ObjectId> {

    private static final ALogger     LOGGER                = Logger.of(OrgMemberDAO.class);

    public static final OrgMemberDAO INSTANCE              = new OrgMemberDAO();

    public static final String       SUPER_ADMIN_MEMBER_ID = "SUPER_ADMIN";

    private OrgMemberDAO() {

        super(OrgMember.class);
    }

    @Async
    public OrgMember getMemberByMemberId(String orgId, String memberId) {

        LOGGER.debug("getMemberByMemberId orgId: " + orgId + ", memberId: " + memberId);
        Query<OrgMember> query = getQuery();
        query.criteria("memberId").equal(memberId.toUpperCase());

        OrgMember orgMember = query.filter("orgId", orgId).retrievedFields(false, OrgMember.FIELD_STATUS).get();


        if (null == orgMember) {
            LOGGER.error("cannot find orgMember for orgId: " + orgId + ", memberId: " + memberId);
        }

        LOGGER.info("getMemberByMemberId found orgMember: " + orgMember);

        return orgMember;
    }

    public OrgMember getMemberByUserId(String orgId, String userId) {

        LOGGER.debug("getMemberByUserId orgId: " + orgId + ", userId: " + userId);

        OrgMember orgMember = getQuery().filter("orgId", orgId).filter("userId", userId).get();

        if (null == orgMember) {
            LOGGER.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userId);
        }

        //LOGGER.info("getMemberByUserId found orgMember: " + orgMember);

        return orgMember;
    }

    public List<OrgMember> getMemberByUserId(String orgId, List<String> userIds) {

        LOGGER.debug("getMemberByUserId orgId: " + orgId + ", userId: " + userIds);

        Query<OrgMember> query = getQuery();

        query.field("orgId").equal(orgId);
        query.field("userId").in(userIds);
        List<OrgMember> orgMembers = query.asList();

        if (null == orgMembers) {
            LOGGER.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userIds);
        }

        //LOGGER.info("getMemberByUserId found orgMember: " + orgMembers);

        return orgMembers;
    }

    public Interval getMemberCurrentActivationPeriod(String orgId, String userId) {

        LOGGER.debug("getMemberActivationPeriods orgId: " + orgId + ", userId: " + userId);
        Query<OrgMember> query = getQuery();
        query.field("orgId").equal(orgId);
        query.field("userId").equal(userId);
        OrgMember orgMember = query.get();
        Interval interval = orgMember.interval;
        return interval;
    }

    public Map<String, OrgMember> getMemberMapByUserId(String orgId, List<String> userIds) {

        List<OrgMember> orgMembers = getMemberByUserId(orgId, userIds);
        return getMemberMapByUserId(orgMembers);

    }

    private static Map<String, OrgMember> getMemberMapByUserId(List<OrgMember> orgMembers) {

        if (CollectionUtils.isEmpty(orgMembers)) {
            return new HashMap<String, OrgMember>();
        }
        Map<String, OrgMember> orgMemberMap = new HashMap<String, OrgMember>();
        for (OrgMember orgMember : orgMembers) {
            orgMemberMap.put(orgMember.userId, orgMember);
        }
        return orgMemberMap;
    }

    // Should be used for NON-STUDENTs
    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
            String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, String countryCode, boolean canImpersonate, Interval interval, String extUserId,
            List<OrgMemberExtraInfo> extraInfo) throws VedantuException {

        return addMember(orgId, userId, memberId, firstName, lastName, dob, gender, email, profile,
                contactNumber,countryCode, null, null, null, StringUtils.EMPTY, canImpersonate, interval,
                extUserId, extraInfo, null);
    }

    // Should be used only for STUDENTs
    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
            String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, String countryCode, MemberParentInfo father, MemberParentInfo mother,
            MemberParentInfo guardian, String parentEmail, Interval interval, String extUserId,
            List<OrgMemberExtraInfo> extraInfo, String referrerCode)
            throws VedantuException {

        return addMember(orgId, userId, memberId, firstName, lastName, dob, gender, email, profile,
                contactNumber,countryCode, father, mother, guardian, parentEmail, false, interval, extUserId,
                extraInfo, referrerCode);

    }

    public OrgMember addMember(String orgId, String userId, String memberId, String firstName,
            String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, String countryCode, MemberParentInfo father, MemberParentInfo mother,
            MemberParentInfo guardian, String parentEmail, boolean canImpersonate,
            Interval interval, String extUserId, List<OrgMemberExtraInfo> extraInfo,
            String referrerCode) throws VedantuException {

        LOGGER.debug("addMember orgId: " + orgId + ", userId: " + userId + ", memberId" + memberId
                + ", firstName: " + firstName + ", lastName:" + lastName + ", dob: " + dob
                + ", gender" + gender + ", email" + email + ", profile" + profile
                + ", contactNumber: " + contactNumber + ", fathe: " + father + ", mother: "
                + mother + ", guardian: " + guardian + ", parentEmail: " + parentEmail
                + "Interval From: " + interval.getFrom() + "Interval Till: " + interval.getTill());

        OrgMember orgMember = getQuery().filter("orgId", orgId).filter("memberId", memberId).get();

        if (null != orgMember) {
            LOGGER.error("cannot add orgMember as orgMember already exists for orgId: " + orgId
                    + ", memberId: " + memberId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_ALREADY_EXISTS);
        }

        orgMember = new OrgMember();
        orgMember.userId = userId;
        orgMember.orgId = orgId;
        orgMember.memberId = memberId;
        orgMember.firstName = firstName;
        orgMember.lastName = lastName;
        orgMember.dob = dob;
        orgMember.gender = gender;
        orgMember.email = email;
        orgMember.profile = profile;
        orgMember.contactNumber = contactNumber;
        orgMember.countryCode = countryCode;
        orgMember.father = father;
        orgMember.mother = mother;
        orgMember.guardian = guardian;
        orgMember.parentEmail = parentEmail;
        orgMember.canImpersonate = canImpersonate;
        orgMember.interval = interval;
        orgMember.extUserId = extUserId;
        orgMember.extraInfo = extraInfo;
        String referralCode = generateReferralCode(firstName);

        if (OrgMemberProfile.STUDENT == orgMember.profile) {
            if (referrerCode != null) {
                String referrerUserId = OrgMemberDAO.INSTANCE
                        .getUserIdFromReferralCode(referrerCode.toLowerCase());
                if (referrerUserId != null && !referrerUserId.isEmpty()) {
                    orgMember.referrerUserId = referrerUserId;
                    addRewards(referrerUserId, orgMember, CampaignType.REFERRAL);
                } else {
                    orgMember.referrerCode = referrerCode;
                }
            }
            orgMember.referralCode = referralCode;
        }
        save(orgMember);
        LOGGER.info("addMember added orgMember: " + orgMember);
        return orgMember;
    }

    public boolean addRewards(String referrerUserId, OrgMember friendOrgMember,
            CampaignType campaignType) {

        Campaign campaign = CampaignDAO.INSTANCE.getCampaignWithCampaignType(campaignType);
        if (campaign != null) {
            int friendRewards = campaign.friendRewards;
            int referrerRewards = campaign.referrerRewards;
            OrgMember referrer = OrgMemberDAO.INSTANCE.getByUserId(referrerUserId);
            friendOrgMember.rewards += friendRewards;
            referrer.rewards += referrerRewards;
            save(referrer);
        }
        return true;
    }

//    public boolean addReferral

    public OrgMember getByUserId(String userId) {

        OrgMember referrer = getDS().find(OrgMember.class).filter("userId", userId).get();
        return referrer;

    }

    public void addOrUpdateContactNumber(String userId, String contactNumber, String countryCode){
        OrgMember member = getDS().find(OrgMember.class).filter("userId", userId).get();
        member.contactNumber = contactNumber;
        member.countryCode = countryCode;
        save(member);
    }

    public User getUsersIdByContact(String contactNumber, String countryCode)
            throws VedantuException {
        Query<OrgMember> query = getQuery().filter("contactNumber", contactNumber).filter(
                "countryCode", countryCode);
        List<OrgMember> members = query.asList();
        List<OrgMember> finalMembers = new ArrayList<OrgMember>();
        for (OrgMember mem : members) {
            boolean isVerified = UserDAO.INSTANCE.isPhoneVerified(mem.userId);
            if (isVerified)
                finalMembers.add(mem);
        }

        if (finalMembers.size() == 0 || finalMembers.size() > 1) {
            throw new VedantuException(
                    VedantuErrorCode.DUPLICATE_CONTACTS_EXIST,
                    "Your Contact Number is Not Verified, "
                            + "Please Login Through Your Email/InstituteID and Verify Your Contact Number");
        }
        return UserDAO.INSTANCE.getById(finalMembers.get(0).userId);
    }

    public List<OrgMember> getUsersByOrgId(String orgId, OrgMemberProfile profile, String targetUserId, long lastUpdated){
        DBObject query = new BasicDBObject();
        query.put("orgId", orgId);
        query.put("profile",profile.name());
        if(StringUtils.isNotEmpty(targetUserId)){
            query.put("userId", targetUserId);
        }
        else{
            if(lastUpdated != Long.MIN_VALUE){
                query.put("lastUpdated", new BasicDBObject("$gte",lastUpdated));
            }
        }
        query.put("recordState", VedantuRecordState.ACTIVE.name());
//        LOGGER.error("Query: "+query.toString());
        VedantuDBResult<OrgMember> links = getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, MongoManager.getSortQuery("lastUpdated", SortOrder.ASC.name()));
        Iterator<OrgMember> orgMembers = links.results.iterator();
        List<OrgMember> orgMembersList = new ArrayList<OrgMember>();
        while(orgMembers.hasNext()){
            orgMembersList.add(orgMembers.next());
        }
        return orgMembersList;
    }

    public String getUserIdFromReferralCode(String referralCode) {

        OrgMember member = getDS().find(OrgMember.class).filter("referralCode", referralCode).get();
        String userId = null;
        if (member != null) {
            userId = member.userId;
        }
        return userId;
    }

    public String generateReferralCode(String firstName) {
        int referralCodeNumber = (int) (Math.random() * 999 + 0);
        String referralString = String.format("%03d", referralCodeNumber);
        String firstNameWithoutSpaces = firstName.replaceAll("\\s+", "");
        String referralCode = firstNameWithoutSpaces.toLowerCase() + referralString;
        boolean isUniqueReferralCode = checkReferralCodeUniqueness(referralCode, firstName);
        if (!isUniqueReferralCode) {
            referralCode = generateReferralCode(firstName);
        }
        return referralCode;
    }

    public void freezeRewards(String userId, long orderId, int rewardDiscount) {
        OrgMember orgMember = getByUserId(userId);
        orgMember.freezedRewards = orgMember.freezedRewards + rewardDiscount;
        orgMember.freezedRewardsOrderId = orderId;
        orgMember.rewards = orgMember.rewards - rewardDiscount;
        save(orgMember);
    }

    public OrgMember addBackRewards(String userId, int lpCreditsRedeemed) {
        OrgMember orgMember = getByUserId(userId);
        orgMember.freezedRewards = orgMember.freezedRewards - lpCreditsRedeemed;
        orgMember.rewards = orgMember.rewards + lpCreditsRedeemed;
        orgMember.freezedRewardsOrderId = 0;
        save(orgMember);
        return getByUserId(userId);
    }

    public OrgMember addBackRewards(String userId) {
        OrgMember orgMember = getByUserId(userId);
        orgMember = addBackRewards(userId, orgMember.freezedRewards);
        return orgMember;
    }

    public void killFreezedRewards(String userId) {
        OrgMember orgMember = getByUserId(userId);
        orgMember.freezedRewards = 0;
        orgMember.freezedRewardsOrderId = 0;
        save(orgMember);
    }

    public boolean checkReferralCodeUniqueness(String referralCode, String firstName) {
        Query<OrgMember> query = getQuery().filter("referralCode", referralCode);
        List<OrgMember> orgMembers = query.asList();
        if (orgMembers.isEmpty()) {
            return true;
        }
        return false;

    }

    // Should be used for NON-STUDENTs
    public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId,
            String firstName, String lastName, String dob, Gender gender, String email,
            OrgMemberProfile profile, String contactNumber, boolean canImpersonate,
            List<OrgMemberExtraInfo> extraInfo, Set<String> updateList) throws VedantuException {

        return updateMember(orgId, userId, orgMemberId, memberId, firstName, lastName, dob, gender,
                email, profile, contactNumber, null, null, null, StringUtils.EMPTY, canImpersonate,
                extraInfo, updateList);
    }

    // Should be used only for STUDENTs
    public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId,
            String firstName, String lastName, String dob, Gender gender, String email,
            OrgMemberProfile profile, String contactNumber, MemberParentInfo father,
            MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,
            List<OrgMemberExtraInfo> extraInfo, Set<String> updateList) throws VedantuException {

        return updateMember(orgId, userId, orgMemberId, memberId, firstName, lastName, dob, gender,
                email, profile, contactNumber, father, mother, guardian, parentEmail, false,
                extraInfo, updateList);
    }

    public OrgMember updateMember(String orgId, String userId, String orgMemberId, String memberId,
            String firstName, String lastName, String dob, Gender gender, String email,
            OrgMemberProfile profile, String contactNumber, MemberParentInfo father,
            MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,
            boolean canImpersonate, List<OrgMemberExtraInfo> extraInfo, Set<String> updateList)
            throws VedantuException {

        LOGGER.debug("updateMember orgId: " + orgId + ", userId: " + userId + ", orgMemberId: "
                + orgMemberId + ", memberId" + memberId + ", firstName: " + firstName
                + ", lastName:" + lastName + ", dob: " + dob + ", gender" + gender + ", email"
                + email + ", profile" + profile + ", contactNumber: " + contactNumber);

        OrgMember orgMember = getMemberByUserId(orgId, userId);

        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(orgMember._getStringId(), orgMemberId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID, "orgMemberId is invalid");
        }

        orgMember.memberId = memberId;
        orgMember.firstName = firstName;
        orgMember.lastName = lastName;
        orgMember.dob = dob;
        orgMember.gender = gender;
        orgMember.email = email;
        orgMember.profile = profile;
        orgMember.contactNumber = contactNumber;
        orgMember.father = father;
        orgMember.mother = mother;
        orgMember.guardian = guardian;
        orgMember.parentEmail = parentEmail;
        orgMember.canImpersonate = canImpersonate;
        orgMember.extraInfo = extraInfo;

        if (CollectionUtils.isNotEmpty(updateList)) {
            updateModel(orgMember, new ArrayList<String>(updateList));
        } else {
            save(orgMember);
        }
        LOGGER.info("updatedMember updated orgMember: " + orgMember);

        return getMemberByUserId(orgId, userId);
    }

    public OrgMember updateOrgMemberEmail(String orgId, String userId, String email) throws VedantuException {

        LOGGER.debug("updateOrgMemberEmail orgId: " + orgId + ", userId: " + userId + ", email"+ email);

        OrgMember orgMember = getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        orgMember.email = email;
        save(orgMember);
        return getMemberByUserId(orgId, userId);
    }

    // private OrgMemberState getMemberActivationState(OrgMember orgMember)
    // {
    // long currentTime = new Date().getTime();
    // if(orgMember.interval.getFrom() < currentTime && orgMember.interval.getTill() > currentTime)
    // {
    // return OrgMemberState.ACTIVE;
    // }
    // else
    // {
    // return OrgMemberState.BLOCKED;
    // }
    // }

    // public OrgMember updateMemberActivationStatus(String orgId, String targetOrgMemberId, String
    // targetUserId, OrgMemberState userState, long activationDate) throws VedantuException {
    //
    // LOGGER.debug("updateMemberActivationStatus OrgMemberState: " + userState +
    // ", activationDate: " + activationDate);
    // long currentTime = new Date().getTime();
    // OrgMember orgMember = getMemberByUserId(orgId, targetUserId);
    //
    // if (null == orgMember) {
    // throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
    // }
    // if (!StringUtils.equals(orgMember._getStringId(), targetOrgMemberId)) {
    // throw new VedantuException(VedantuErrorCode.INVALID_ID, "orgMemberId is invalid");
    // }
    //
    // if(activationDate < new Date().getTime())
    // {
    // throw new VedantuException(VedantuErrorCode.DATE_TIME_LESS_THAN_CURRENT,
    // "Date time belongs to past");
    // }
    //
    // if(userState == OrgMemberState.ACTIVE)
    // {
    // if(orgMember.interval.getFrom() < currentTime && orgMember.interval.getTill() > currentTime)
    // {
    // throw new VedantuException(VedantuErrorCode.USER_ALREADY_ACTIVE);
    // }
    //
    // if(orgMember.interval.getFrom() < currentTime && orgMember.interval.getTill() < currentTime)
    // {
    // //Insert to and From in log table
    // orgMember.interval.setFrom(activationDate);
    // orgMember.interval.setTill(-1);
    // }
    //
    // if(orgMember.interval.getFrom() > currentTime && orgMember.activeTill > currentTime)
    // {
    // orgMember.interval.setFrom(activationDate);
    // }
    // }
    //
    // else if(userState == OrgMemberState.BLOCKED)
    // {
    // if(orgMember.interval.getFrom() < currentTime && orgMember.interval.getTill() > currentTime)
    // {
    // orgMember.interval.setTill(activationDate);
    // }
    //
    // if(orgMember.interval.getFrom() < currentTime && orgMember.interval.getTill() < currentTime)
    // {
    // throw new
    // VedantuException(VedantuErrorCode.USER_ALREADY_DEACTIVATED_WITH_NO_FUTURE_ACTIVATION);
    // }
    //
    // if(orgMember.interval.getFrom() > currentTime && orgMember.interval.getTill() > currentTime)
    // {
    // orgMember.interval.setTill(activationDate);
    // }
    // }
    //
    // save(orgMember);
    //
    // LOGGER.info("updatedMember updated orgMember: " + orgMember);
    //
    // return getMemberByUserId(orgId, targetUserId);
    // }

    public List<OrgMember> getAssociatedOrgs(String userId) {

        LOGGER.debug("getAssociatedOrgs userId: " + userId);

        List<OrgMember> orgMembers = getQuery().filter("userId", userId).asList();
        LOGGER.info("getAssociatedOrgs found associated organizations: {"
                + StringUtils.join(orgMembers, ", ") + "}");

        return orgMembers;
    }

    public OrgMemberMappingInfo getMemberMappingForSection(String orgId, String userId,
            String sectionId) throws VedantuException {
        OrgMember orgMember = getMemberByUserId(orgId, userId);

        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        for (OrgMemberMappingInfo mInfo : orgMember.mappings) {
            if (mInfo.sectionId.equals(sectionId)) {
                return mInfo;
            }
        }
        return null;
    }

    public OrgMember addOrgMemberMapping(String orgId, String userId, String orgMemberId,
            String programId, String centerId, Set<String> sectionIds, Set<String> courseIds,
            MutableBoolean isAdded, boolean noExceptionOnExistingMapping) throws VedantuException {

        return addOrgMemberMapping(orgId, userId, orgMemberId, programId, centerId, sectionIds,
                courseIds, isAdded, noExceptionOnExistingMapping, null, 0);
    }

    public OrgMember addOrgMemberMapping(String orgId, String userId, String orgMemberId,
            String programId, String centerId, Set<String> sectionIds, Set<String> courseIds,
            MutableBoolean isAdded, boolean noExceptionOnExistingMapping, String transactionId,
            int packageDays)
            throws VedantuException {

        LOGGER.debug("addOrgMemberMapping orgId: " + orgId + ", userId: " + userId
                + ", orgMemberId: " + orgMemberId + ", programId: " + programId + ", centerId: "
                + centerId + ", sectionIds: {" + StringUtils.join(sectionIds, ", ")
                + "}, courseIds: {" + StringUtils.join(courseIds, ", ") + "}, isAdded: " + isAdded
                + ", noExceptionOnExistingMapping: " + noExceptionOnExistingMapping);

        OrgMember orgMember = getMemberByUserId(orgId, userId);

        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        if (!StringUtils.equals(orgMember._getStringId(), orgMemberId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID, "found orgMember["
                    + orgMember._getStringId() + "] and provide orgMemberId[" + orgMemberId
                    + "] are different");
        }

        LOGGER.debug("iterating through sectionIds");

        boolean addedMapping = false;
        for (String sectionId : sectionIds) {

            LOGGER.debug("for sectionId: " + sectionId);

            OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId,
                    centerId, sectionId, courseIds);

            if (CollectionUtils.isNotEmpty(orgMember.mappings)
                    && orgMember.mappings.contains(orgMemberMappingInfo)) {
                LOGGER.error("orgMemberMapping already exists for orgId: " + orgId + ", userId: "
                        + userId + ", orgMemberId: " + orgMemberId + ", programId: " + programId
                        + ", centerId: " + centerId + ", sectionId: " + sectionId);
                if (!noExceptionOnExistingMapping) {
                    throw new VedantuException(
                            VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_ALREADY_EXISTS);
                }
                else {
                    //To indicate whether user already has this section
                    addedMapping = true;
                }
            } else {
                SrcEntity item = new SrcEntity(EntityType.SECTION, sectionId);

                addedMapping = orgMember.add(orgMemberMappingInfo);
                if (addedMapping) {
                    // when a user is added to paid section/programe then sectionIds.size() will be
                    // ==
                    // 1,
                    // which we already have verified before this API call
                    orgMemberMappingInfo.orderId = StringUtils.isEmpty(transactionId) ? StringUtils.EMPTY
                            : OrderManager.markTransactionConmpleted(transactionId, item);
                    orgMemberMappingInfo.timeJoined = System.currentTimeMillis();
                    if (packageDays > 0) {
                        orgMemberMappingInfo.endTime = orgMemberMappingInfo.timeJoined + TimeUnit.DAYS.toMillis(packageDays);
                    }
                }
                LOGGER.info("added orgMemberMappingInfo: " + orgMemberMappingInfo);
            }
        }
        if (addedMapping) {
            save(orgMember);
            LOGGER.info("addOrgMemberMapping saved orgMember: " + orgMember);
        }

        isAdded.setValue(addedMapping);

        return orgMember;
    }

    public OrgMember updateOrgMemberMapping(String orgId, String userId, String orgMemberId,
            String programId, String centerId, Set<String> sectionIds, Set<String> addCourseIds,
            Set<String> removeCourseIds, MutableBoolean isUpdated) throws VedantuException {

        LOGGER.debug("updateOrgMemberMapping orgId: " + orgId + ", userId: " + userId
                + ", orgMemberId: " + orgMemberId + ", programId: " + programId + ", centerId: "
                + centerId + ", sectionIds: {" + StringUtils.join(sectionIds, ", ")
                + "}, addCourseIds: {" + StringUtils.join(addCourseIds, ", ")
                + "}, removeCourseIds: {" + StringUtils.join(removeCourseIds, ", ")
                + "}, isUpdated: " + isUpdated);

        OrgMember orgMember = getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(orgMember._getStringId(), orgMemberId)) {
            LOGGER.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match orgMemberId: " + orgMemberId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        if (CollectionUtils.isEmpty(orgMember.mappings)) {
            LOGGER.debug("no mappings found");
            return orgMember;
        }

        boolean updated = false;
        LOGGER.debug("iterating through sectionIds");

        for (String sectionId : sectionIds) {

            LOGGER.debug("for sectionId: " + sectionId);

            OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId,
                    centerId, sectionId, null);
            LOGGER.debug("looking for orgMemberMappingInfo: " + orgMemberMappingInfo);

            OrgMemberMappingInfo infoToModify = null;

            for (OrgMemberMappingInfo info : orgMember.mappings) {
                if (null == info) {
                    continue;
                }
                if (orgMemberMappingInfo.equals(info)) {
                    infoToModify = info;
                    break;
                }
            }
            if (null == infoToModify) {
                LOGGER.debug("corresponding mapping not found");
                continue;
            }

            LOGGER.debug("corresponding mapping to be modified infoToModify: " + infoToModify);

            infoToModify.addCourses(addCourseIds);
            infoToModify.removeCourses(removeCourseIds);

            LOGGER.debug("after adding and removing courses infoToModify: " + infoToModify);

            if (CollectionUtils.isEmpty(infoToModify.courseIds)) {
                LOGGER.debug("for orgMemberId: " + orgMemberId + ", profile: " + orgMember.profile
                        + " will remove infoToModify: " + infoToModify + " as it has no courses");
                orgMember.remove(infoToModify);
            }

            updated = true;
        }
        if (updated) {
            save(orgMember);
            isUpdated.setValue(true);

            LOGGER.info("updateOrgMemberMapping updated orgMember: " + orgMember);
        } else {
            LOGGER.info("updateOrgMemberMapping not updated");
        }
        return orgMember;
    }

    public OrgMember updateEndDateMapping(String orgId, String userId, String sectionId,
                        long endTime) throws VedantuException {

        OrgMember orgMember = getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(orgMember.mappings)) {
            LOGGER.debug("no mappings found");
            return orgMember;
        }

        OrgMemberMappingInfo mappingToUpdate = null;
        for (OrgMemberMappingInfo info : orgMember.mappings) {
            if (null == info) {
                continue;
            }
            if (sectionId.equals(info.sectionId)) {
                mappingToUpdate = info;
                break;
            }
        }
        if (null == mappingToUpdate) {
            LOGGER.error("corresponding mapping not found updateEndDateMapping for section:" + sectionId);
            return orgMember;
        }

        mappingToUpdate.endTime = endTime;
        save(orgMember);
        return orgMember;
    }

    public OrgMember addSaleDetailsToMapping(String orgId, String userId, String sectionId,
            String saleDetailsId) throws VedantuException {
        OrgMember orgMember = getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(orgMember.mappings)) {
            LOGGER.debug("no mappings found");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
        }

        OrgMemberMappingInfo mappingToUpdate = null;
        for (OrgMemberMappingInfo info : orgMember.mappings) {
            if (null == info) {
                continue;
            }
            if (sectionId.equals(info.sectionId)) {
                mappingToUpdate = info;
                break;
            }
        }
        if (null == mappingToUpdate) {
            LOGGER.error("corresponding mapping not found addSaleDetails for section:" + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
        }
        mappingToUpdate.saleDetailsId = saleDetailsId;
        save(orgMember);
        return orgMember;
    }

    public OrgMember removeOrgMemberMapping(String orgId, String userId, String orgMemberId,
            String programId, String centerId, Set<String> sectionIds, MutableBoolean isRemoved)
            throws VedantuException {

        LOGGER.debug("removeOrgMemberMapping orgId: " + orgId + ", userId: " + userId
                + ", orgMemberId: " + orgMemberId + ", programId: " + programId + ", centerId: "
                + centerId + ", sectionIds: {" + StringUtils.join(sectionIds, ", ")
                + "}, isRemoved: " + isRemoved);

        OrgMember orgMember = getMemberByUserId(orgId, userId);
        if (null == orgMember) {
            LOGGER.error("orgMember not found");
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }
        if (!StringUtils.equals(orgMember._getStringId(), orgMemberId)) {
            LOGGER.error("orgMember._id: " + orgMember._getStringId()
                    + " does not match orgMemberId: " + orgMemberId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }

        LOGGER.debug("iterating through sectionIds");

        for (String sectionId : sectionIds) {

            LOGGER.debug("for sectionId: " + sectionId);

            OrgMemberMappingInfo orgMemberMappingInfo = new OrgMemberMappingInfo(programId,
                    centerId, sectionId, null);
            LOGGER.debug("looking for orgMemberMappingInfo: " + orgMemberMappingInfo);

            OrgMemberMappingInfo removedMapping = orgMember.remove(orgMemberMappingInfo);
            LOGGER.debug("removedMapping: " + removedMapping);

            if (null == removedMapping) {
                LOGGER.error("orgMemberMapping not found for orgId: " + orgId + ", userId: "
                        + userId + ", orgMemberId: " + orgMemberId + ", programId: " + programId
                        + ", centerId: " + centerId + ", sectionId: " + sectionId);
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
            }

        }
        save(orgMember);
        isRemoved.setValue(true);

        LOGGER.info("removeOrgMemberMapping updated orgMember: " + orgMember);

        return orgMember;
    }

    public void updateOrgMemberExpiredMappings(String orgId, String userId) {
        OrgMember member = getMemberByUserId(orgId, userId);
        if (null == member) {
            LOGGER.error("orgMember not found");
            return;
        }
        LOGGER.debug("Updating expired mappings for member with userId: " + userId);
        boolean modified = false;
        if (member.expiredMappings == null) {
            member.expiredMappings = new ArrayList<OrgMemberMappingInfo>();
            modified = true;
        }
        if (member.mappings != null && !member.mappings.isEmpty()) {
            Iterator<OrgMemberMappingInfo> mappingIterator = member.mappings.iterator();
            while (mappingIterator.hasNext()) {
                OrgMemberMappingInfo mapping = mappingIterator.next();
                if (mapping.endTime > 0 && mapping.endTime < System.currentTimeMillis()) {
                    // This mapping has expired. Add to expiredMappingsList
                    member.expiredMappings.add(mapping);
                    LOGGER.debug("Mapping has expired for Section:" + mapping.sectionId);
                    mappingIterator.remove();
                    modified = true;
                }
            }
        }
        if (modified) {
            save(member);
        }
    }

    public List<OrgMember> getOrgMembers(String orgId, OrgMemberProfile profile, String programId,
            String centerId, String sectionId, String courseId, String queryText, int start,
            int size, MutableLong hits) {

        return getOrgMembers(orgId, profile, null, programId, centerId, sectionId, courseId,
                queryText, start, size, null, null, hits);
    }

    public List<OrgMember> getOrgMembers(String orgId, OrgMemberProfile profile,
            List<OrgMemberProfile> excludeProfiles, String programId, String centerId,
            String sectionId, String courseId, String queryText, int start, int size,
            List<String> excludeUserIds, Boolean canImpersonate, MutableLong hits) {

        return getOrgMembers(orgId, profile, excludeProfiles, programId, centerId, sectionId,
                courseId, queryText, start, size, excludeUserIds, null, canImpersonate, hits);
    }

    public List<OrgMember> getOrgMembers(String orgId, OrgMemberProfile profile,
            List<OrgMemberProfile> excludeProfiles, String programId, String centerId,
            String sectionId, String courseId, String queryText, int start, int size,
            List<String> excludeUserIds, List<String> includeUserIds, Boolean canImpersonate,
            MutableLong hits) {

        LOGGER.debug("getOrgMembers orgId: " + orgId + ", profiles: " + profile + ", programId: "
                + programId + ", centerId: " + centerId + ", sectionId: " + sectionId
                + ", courseId: " + courseId + " exclude profiles " + excludeProfiles
                + " excludeUserIds " + excludeUserIds + ", includeUserIds:" + includeUserIds);
        Query<OrgMember> query = getQuery().filter("orgId", orgId);

        // This is one is done because of this issue
        // http://code.google.com/p/morphia/issues/detail?id=225
        // so if profile exists we precisely use that one otherwise we exclude all excludeprofiles
        if (profile != null) {
            query.field("profile").hasAllOf(Arrays.asList(profile));

        } else if (CollectionUtils.isNotEmpty(excludeProfiles)) {
            query.field("profile").hasNoneOf(excludeProfiles);
        }

        if (StringUtils.isNotEmpty(queryText)) {
            query.or(
                    query.criteria(ConstantsGlobal.FIRST_NAME).startsWithIgnoreCase(
                            queryText.trim()), query.criteria(ConstantsGlobal.LAST_NAME)
                            .startsWithIgnoreCase(queryText.trim()),
                    query.criteria(ConstantsGlobal.EMAIL)
                            .startsWithIgnoreCase(queryText.trim()),
                    query.criteria(ConstantsGlobal.MEMBER_ID)
                            .startsWithIgnoreCase(queryText.trim()));
        }

        if (StringUtils.isNotEmpty(programId)) {
            query.filter("mappings.programId", programId);
        }
        if (StringUtils.isNotEmpty(centerId)) {
            query.filter("mappings.centerId", centerId);
        }
        if (StringUtils.isNotEmpty(sectionId)) {
            query.filter("mappings.sectionId", sectionId);
        }
        if (StringUtils.isNotEmpty(courseId)) {
            query.filter("mappings.courseIds", courseId);
        }

        if (CollectionUtils.isNotEmpty(excludeUserIds)) {
            query.field("userId").hasNoneOf(excludeUserIds);
        }

        if (CollectionUtils.isNotEmpty(includeUserIds)) {
            query.field("userId").hasAnyOf(includeUserIds);
        }

        if (canImpersonate != null) {
            query.field("canImpersonate").equal(canImpersonate.booleanValue());
        }

        query.order("firstName,lastName");
        query.offset(start).limit(size);
        LOGGER.debug("query: " + query);

        List<OrgMember> orgMembers = query.asList();
        if (hits != null) {
            hits.setValue(query.countAll());
        }
        return orgMembers;
    }

    public OrgMember getOrgMemberWithEmail(String orgId, String email) {

        Query<OrgMember> query = getQuery();
        query.criteria("email").equal(email);

        OrgMember orgMember = query.filter("orgId", orgId)
                .retrievedFields(false, OrgMember.FIELD_STATUS).get();

        if (null == orgMember) {
            LOGGER.error("cannot find orgMember for orgId: " + orgId + ", email: " + email);
        }

        return orgMember;
    }

    public long getCountOfMembers(String orgId, OrgMemberProfile profile,
            String programId, String centerId,String sectionId) {
        Query<OrgMember> query = getQuery().filter("orgId", orgId);
        if (profile != null) {
            query.field("profile").hasAllOf(Arrays.asList(profile));
        }
        if (StringUtils.isNotEmpty(programId)) {
            query.filter("mappings.programId", programId);
        }
        if (StringUtils.isNotEmpty(centerId)) {
            query.filter("mappings.centerId", centerId);
        }
        if (StringUtils.isNotEmpty(sectionId)) {
            query.filter("mappings.sectionId", sectionId);
        }
        long count = query.countAll();
        return count;
    }

    public Set<String> getExistingMemberIds(String orgId, Collection<String> memberIds) {

        Set<String> existingMemberIds = new HashSet<String>();

        Set<String> upercaseMemberIds = new HashSet<String>();
        for (String memberId : memberIds) {
            upercaseMemberIds.add(StringUtils.upperCase(memberId).trim());
        }
        DBObject query = new BasicDBObject("orgId", orgId);
        query.put("memberId", new BasicDBObject(MongoManager.IN_QUERY, upercaseMemberIds.toArray()));
        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList("memberId"),
                MongoManager.INCLUDE_FIELD);
        VedantuDBResult<OrgMember> tResults = getInfos(query, fields, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        for (OrgMember t : tResults.results) {
            existingMemberIds.add(t.memberId);
        }
        return existingMemberIds;
    }

    public boolean updateLoginStatus(String userId, LoginStatus status) throws VedantuException {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getByUserId(userId);
        if (orgMember.status != null) {
            if (orgMember.status.size() > 0) {
                Iterator<LoginStatus> statusIterator = orgMember.status.iterator();
                while (statusIterator.hasNext()) {
                    LoginStatus previousStatus = statusIterator.next();
                    if (!previousStatus.deviceId.equals(status.deviceId)) {
                        if (previousStatus.status.equals(UserStatus.LOGGED_IN)) {
                            previousStatus.status = UserStatus.LOGGED_OUT;
                            previousStatus.logoutTime = System.currentTimeMillis();
                        }
                    }
                    OrgMemberDAO.INSTANCE.save(orgMember);
                }
            }
        }
        DBObject query = new BasicDBObject("userId", userId);
        DBObject updateQuery = new BasicDBObject();
        DBObject addToStatusQuery = new BasicDBObject();
        addToStatusQuery.put("status", ObjectMapperUtils.convertValue(status, Map.class));
        updateQuery.put(MongoManager.PUSH_QUERY, addToStatusQuery);
        return this.update(query, updateQuery, false, true);


    }

    public boolean updateLogoutStatus(String userId, LoginStatus status) throws VedantuException {

        if (!checkIfLoggedIn(userId, status.deviceId, status.deviceType)) {
            throw new VedantuException(VedantuErrorCode.ALREADY_LOGGED_OUT);
        }

        List<OrgMember> orgMemberStatus = getStatuses(userId, status.deviceId, status.deviceType);
        LOGGER.info("Current statuses " + orgMemberStatus);

        DBObject query = new BasicDBObject("userId", userId);
        DBObject updateQuery = new BasicDBObject();
        DBObject matchQuery = new BasicDBObject();
        matchQuery.put("deviceId", status.deviceId);
        matchQuery.put("deviceType", status.deviceType);
        DBObject addToStatusQuery = new BasicDBObject();

        addToStatusQuery.put("status", matchQuery);
        updateQuery.put(MongoManager.PULL_QUERY, addToStatusQuery);
        boolean result = this.update(query, updateQuery, false, true);

        if (!result) {
            return result;
        }
        if (CollectionUtils.isNotEmpty(orgMemberStatus)) {
            LOGGER.info("After logging out analysing and updating statuses" + status);
            List<LoginStatus> statuses = orgMemberStatus.get(0).status;
            for (LoginStatus currentStatus : statuses) {
                if (currentStatus.deviceId.equals(status.deviceId)) {
                    status.callingApp = currentStatus.callingApp;
                    status.callingAppId = currentStatus.callingAppId;
                    status.callingUserId = currentStatus.callingUserId;
                    status.expiryTime = currentStatus.expiryTime;
                    status.loginTime = currentStatus.loginTime;
                    status.logoutTime = System.currentTimeMillis();
                    status.status = UserStatus.LOGGED_OUT;
                    LOGGER.info("Record will be removed and added to Logins" + status);
                    break;
                }
            }
        }
        return result;
    }

    public List<OrgMember> getStatuses(String userId, String deviceId, String deviceType) {

        DBObject query = new BasicDBObject("userId", userId);
        if (StringUtils.isNotEmpty(deviceId)) {
            query.put("status.deviceId", deviceId);
        }
        if (StringUtils.isNotEmpty(deviceType)) {
            query.put("status.deviceType", deviceType);
        }

        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList("status"),
                MongoManager.INCLUDE_FIELD);
        VedantuDBResult<OrgMember> tResults = getInfos(query, fields, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(tResults.results)) {
            return tResults.results;
        }
        return null;
    }


	public List<OrgMember> getStatus(String userId) {

        DBObject query = new BasicDBObject("userId", userId);


        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList("status"),
                MongoManager.INCLUDE_FIELD);
        VedantuDBResult<OrgMember> tResults = getInfos(query, fields, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(tResults.results)) {
            return tResults.results;
        }
        return null;
    }


    public boolean checkIfLoggedIn(String userId, String deviceId, String deviceType) {

        DBObject query = new BasicDBObject("userId", userId);
        if (StringUtils.isNotEmpty(deviceId)) {
            query.put("status.deviceId", deviceId);
        }
        if (StringUtils.isNotEmpty(deviceType)) {
            query.put("status.deviceType", deviceType);
        }

        // DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList("status",FIELD_ID),
        // MongoManager.INCLUDE_FIELD);
        VedantuDBResult<OrgMember> tResults = getInfos(query, null, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(tResults.results)) {
            LOGGER.debug("Results:" + tResults.results);
            return true;
        }
        return false;
    }


    public boolean checkIfLoggedInDB(String userId, String deviceId) {

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getByUserId(userId);
        if (orgMember.status != null) {
            if (orgMember.status.size() > 0) {
                Iterator<LoginStatus> statusIterator = orgMember.status.iterator();
                while (statusIterator.hasNext()) {
                    LoginStatus status = statusIterator.next();
                    if (status.deviceId.equals(deviceId)) {
                        if (status.status.equals(UserStatus.LOGGED_IN)) {
                            return true;
                        } else if (status.status.equals(UserStatus.LOGGED_OUT)) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

	 public boolean isValidReferralCode(String referralCode) {
	        OrgMember member = getQuery().filter("referralCode", referralCode).get();
	        if(member == null){
	            LOGGER.info(referralCode+" is invalid referralCode");
	            return false;
	        }
	        LOGGER.info(referralCode+" is already existing referralCode");
	        return true;
	    }

    public List<OrgMember> getOnlineUsers(String orgId, OrgMemberProfile profile, String programId,
            String centerId, String sectionId, String queryText, int start, int size,
            org.apache.commons.lang.mutable.MutableLong hits, List<String> deviceTypes,
            UserStatus loggedIn) {

        Query<OrgMember> query = getQuery().filter("orgId", orgId);
        if (null != profile) {
            query.filter("profile", profile);
        }

        if (StringUtils.isNotEmpty(queryText)) {
            query.or(
                    query.criteria(ConstantsGlobal.FIRST_NAME).startsWithIgnoreCase(
                            queryText.trim()), query.criteria(ConstantsGlobal.LAST_NAME)
                            .startsWithIgnoreCase(queryText.trim()),
                    query.criteria(ConstantsGlobal.MEMBER_ID)
                            .startsWithIgnoreCase(queryText.trim()));
        }

        if (StringUtils.isNotEmpty(programId)) {
            query.filter("mappings.programId", programId);
        }
        if (StringUtils.isNotEmpty(centerId)) {
            query.filter("mappings.centerId", centerId);
        }
        if (StringUtils.isNotEmpty(sectionId)) {
            query.filter("mappings.sectionId", sectionId);
        }

        query.order("firstName,lastName");
        query.offset(start).limit(size);
        LOGGER.debug("query: " + query);

        // findQuery.field("status.deviceId").hasAnyOf(
        if (loggedIn == UserStatus.LOGGED_IN && CollectionUtils.isNotEmpty(deviceTypes)) {

            query.field("status.deviceType").hasAnyOf(deviceTypes);

        } else if (loggedIn == UserStatus.LOGGED_OUT && CollectionUtils.isNotEmpty(deviceTypes)) {

            query.field("status.deviceType").hasNoneOf(deviceTypes);

        }

        query.order("firstName,lastName");
        query.offset(start).limit(size);
        LOGGER.debug("query: " + query);

        List<OrgMember> orgMembers = query.asList();
        LOGGER.info("getOrgMembers orgMembers: {" + StringUtils.join(orgMembers, ", ") + "}");
        if (hits != null) {
            hits.setValue(query.countAll());
        }
        return orgMembers;

    }

    public void removeSharedProgramMapping(String subscriberOrgId, String programId) {
        Query<OrgMember> queryMembers = getDS().find(entityClazz).filter("orgId", subscriberOrgId);
        queryMembers.filter("mappings.programId", programId).get();
        List<OrgMember> orgMembers = queryMembers.asList();

        for (OrgMember member : orgMembers) {
            boolean modified = false;
            if (member.mappings != null && !member.mappings.isEmpty()) {
                Iterator<OrgMemberMappingInfo> mappingIterator = member.mappings.iterator();
                while (mappingIterator.hasNext()) {
                    OrgMemberMappingInfo mapping = mappingIterator.next();
                    if (mapping.programId.equals(programId)) {
                        LOGGER.warn("Removing mapping because of shared organization deletion for userId : "
                                + member.userId);
                        // Other organization stopped sharing so moving it to
                        // expired mappings
                        if (member.expiredMappings == null) {
                            member.expiredMappings = new ArrayList<OrgMemberMappingInfo>();
                        }
                        member.expiredMappings.add(mapping);
                        mappingIterator.remove();
                        modified = true;
                    }
                }
            }
            if (modified) {
                save(member);
            }
        }
    }

    public OrgMember getByUserPhone(String phone, String countryCode){
        OrgMember member = getDS().find(OrgMember.class).filter("contactNumber", phone)
                .filter("countryCode", countryCode).get();
        return member;
    }

    public boolean checkIfUserEmailExists(String contactNumber, String countryCode) {
        OrgMember member = getByUserPhone(contactNumber,countryCode);
        if(member != null){
            if(member.email != null && !member.email.equals(StringUtils.EMPTY))
                return true;
            LOGGER.info("User email not found for contact number : "+contactNumber);
            return false;
        }
        LOGGER.info("User doesn't exist for contact number : "+contactNumber);
        return false;
    }

    public boolean isNewPhone(String contactNumber, String countryCode) {
        OrgMember member = getByUserPhone(contactNumber,countryCode);
        if(member == null){
            LOGGER.info(contactNumber+" is new contact number");
            return true;
        }
        LOGGER.info(contactNumber+" is already existing contact number");
        return false;
    }

    public List<OrgMember> getStumagzUsers() {
        OrgMember member = getQuery().filter("referralCode", "stumagz").get();
        return getReferredUsers(member.userId);
    }

    public List<OrgMember> getReferredUsers(String userId) {
        return getQuery().filter("referrerUserId", userId).asList();
    }

    public OrgMember getOrgMemberFromReferralCode(String referralCode) {
        OrgMember member = getDS().find(OrgMember.class).filter("referralCode", referralCode).get();
        return member;
    }

    public List<OrgMember> getStudents(String orgId, Long startDate, Long endDate) {
        Query<OrgMember> query = getQuery();
        query = query.field("orgId").equal(orgId);
        query = query.field("timeCreated").greaterThanOrEq(startDate);
        query = query.field("timeCreated").lessThanOrEq(endDate);
        query = query.field("recordState").equal(VedantuRecordState.ACTIVE);
        query = query.field("profile").equal(OrgMemberProfile.STUDENT);
        return query.asList();
    }

    public List<OrgMember> getUserIdsFromMemberIdAndNameMatch(String orgId, String queryText) {
        // TODO Auto-generated method stub
        Query<OrgMember> query = getQuery();
        query.field("orgId").equal(orgId);
        query.field("profile").equal(OrgMemberProfile.STUDENT);
        if (StringUtils.isNotEmpty(queryText)) {
            query.or(
                    query.criteria(ConstantsGlobal.FIRST_NAME).startsWithIgnoreCase(
                            queryText.trim()), query.criteria(ConstantsGlobal.LAST_NAME)
                            .startsWithIgnoreCase(queryText.trim()),
                    query.criteria(ConstantsGlobal.MEMBER_ID)
                            .startsWithIgnoreCase(queryText.trim()));
        }
        return query.asList();
    }
}
