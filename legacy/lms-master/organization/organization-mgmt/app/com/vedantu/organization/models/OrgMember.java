package com.vedantu.organization.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.OrgMemberState;
import com.vedantu.organization.pojos.MemberParentInfo;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;
import com.vedantu.organization.pojos.OrgMemberExtraInfo;
import com.vedantu.organization.pojos.OrgMemberMappingExtendedInfo;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.models.LoginStatus;

@Entity(value = "orgmembers", noClassnameStored = true)
@Indexes({
        @Index(value = "orgId, memberId", unique = true),
        @Index(value = "orgId, userId", unique = true),
        @Index(value = "orgId, profile, mappings.programId, mappings.centerId, mappings.sectionId"),
        @Index(value = "orgId, profile, mappings.courseIds") })
public class OrgMember extends VedantuBaseMongoModel {

    public transient static final String FIELD_DOB             = "dob";
    public transient static final String FIELD_FIRST_NAME      = "firstName";
    public transient static final String FIELD_LAST_NAME       = "lastName";
    public transient static final String FIELD_EMAIL           = "email";
    public transient static final String FIELD_PROFILE         = "profile";
    public transient static final String FIELD_CONTACT_NUMBER  = "contactNumber";
    public transient static final String FIELD_MAPPINGS        = "thumbnail";
    public transient static final String FIELD_FATHER          = "father";
    public transient static final String FIELD_MOTHER          = "mother";
    public transient static final String FIELD_GUARDIAN        = "guardian";
    public transient static final String FIELD_PARENT_EMAIL    = "parentEmail";
    public transient static final String FIELD_CAN_IMPERSONATE = "canImpersonate";
    public transient static final String FIELD_STATUS          = "status";
    public transient static final String FIELD_GENDER          = "gender";
    public transient static final String FIELD_EXTRA_INFO      = "extraInfo";

    public transient static final String FIELD_MEMBER_ID       = "memberId";

    @Indexed
    public String                        userId;
    public String                        orgId;
    public String                        memberId;
    public String                        firstName;
    public String                        lastName;
    public String                        dob;
    public Gender                        gender;
    public String                        email;
    public OrgMemberProfile              profile;

    @Indexed
    public String                        contactNumber;
    public String                        countryCode;
    public Set<OrgMemberMappingInfo>     mappings;
    public List<OrgMemberMappingInfo>    expiredMappings;
    public String                        thumbnail;

    // Following fields are applicable only for STUDENT
    public MemberParentInfo              father;
    public MemberParentInfo              mother;
    public MemberParentInfo              guardian;
    public String                        parentEmail;
    public boolean                       canImpersonate        = false;
    public List<LoginStatus>             status;
    public Interval                      interval;

    public AuthType                      authType;
    public String                        extUserId;

    public List<OrgMemberExtraInfo>      extraInfo;

    @Indexed
    public String                        referralCode;
    public String                        referrerUserId;
    public int                           rewards;
    public String                        referrerCode;
    public int                           freezedRewards;
    public long                          freezedRewardsOrderId;

    public boolean add(OrgMemberMappingInfo mapping) {

        if (null == mappings) {
            mappings = new HashSet<OrgMemberMappingInfo>();
        }
        boolean added = true;
        if (!mappings.add(mapping)) {
            added = false;
            // if the mapping already present then we need to update the expireTime/endTime of user
            // mapping with specific section
            for (OrgMemberMappingInfo mInfo : mappings) {
                if (mInfo.equals(mapping)) {
                    mInfo.endTime = mapping.endTime;
                }
            }
        }
        return added;
    }

    public OrgMemberMappingInfo _getProgramMapping(String programId) {

        OrgMemberMappingInfo progMapping = null;
        if (mappings == null) {
            return progMapping;
        }
        for (OrgMemberMappingInfo mInfo : mappings) {
            if (StringUtils.equals(programId, mInfo.programId)) {
                progMapping = mInfo;
                break;
            }
        }
        return progMapping;
    }

    public OrgMemberState _getMemberState(long currentTime) {

        long userActiveTill = this.interval.getTill();

        if (userActiveTill == -1) {
            userActiveTill = Long.MAX_VALUE;
        }
        if (this.interval.getFrom() < currentTime && currentTime < userActiveTill) {
            return OrgMemberState.ACTIVE;
        } else {
            return OrgMemberState.BLOCKED;
        }
    }

    public OrgMemberMappingInfo remove(OrgMemberMappingInfo mapping) {

        if (null == mappings || null == mapping) {
            return null;
        }
        Iterator<OrgMemberMappingInfo> mappingIterator = mappings.iterator();
        while (mappingIterator.hasNext()) {
            OrgMemberMappingInfo tMapping = mappingIterator.next();
            if (mapping.equals(tMapping)) {
                mappingIterator.remove();
                return tMapping;
            }
        }
        return null;
    }

    public final String getFullName() {
        if (StringUtils.isEmpty(this.lastName)) {
            return this.firstName;
        }

        return this.firstName + " " + this.lastName;
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        OrgMemberMappingExtendedInfo tMappings = new OrgMemberMappingExtendedInfo();
        OrgMemberExtendedInfo extendedInfo = null;
        if (this.interval == null) {
            this.interval = new Interval(System.currentTimeMillis(), -1);
        }

        if (OrgMemberProfile.STUDENT == profile) {
            extendedInfo = new OrgMemberExtendedInfo(_getStringId(), recordState, firstName,
                    timeCreated, lastUpdated, userId, orgId, memberId, firstName, lastName, dob,
                    gender, email, profile, _getThumbnailUrl(), contactNumber, tMappings, father,
                    mother, guardian, parentEmail, this.canImpersonate, this.interval.getFrom(),
                    this.interval.getTill());
        } else {
            extendedInfo = new OrgMemberExtendedInfo(_getStringId(), recordState, firstName,
                    timeCreated, lastUpdated, userId, orgId, memberId, firstName, lastName, dob,
                    gender, email, profile, _getThumbnailUrl(), contactNumber, tMappings,
                    this.canImpersonate, this.interval.getFrom(), this.interval.getTill());

        }
        // extendedInfo.loginStatus = this.status;
        extendedInfo.extraInfo = this.extraInfo == null ? new ArrayList<OrgMemberExtraInfo>()
                : this.extraInfo;
        long currentTime = new Date().getTime();
        extendedInfo.userState = _getMemberState(currentTime);
        return extendedInfo;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        OrgMemberBasicInfo basicInfo = new OrgMemberBasicInfo(_getStringId(), userId, orgId,
                memberId, firstName, lastName, profile, _getThumbnailUrl(), contactNumber,
                recordState, canImpersonate);
        OrgMemberMappingExtendedInfo tMappings = new OrgMemberMappingExtendedInfo();
        basicInfo.mappings = tMappings;

        return basicInfo;
    }

    public String _getThumbnailUrl() {

        if (StringUtils.isNotEmpty(thumbnail)) {
            return ImageDisplayURLUtil.getEntityThumbnail(EntityType.USER, thumbnail);
        }

        List<String> suffixComponents = new ArrayList<String>();
        suffixComponents.add(profile.name());

        if (OrgMemberProfile.STUDENT == profile || OrgMemberProfile.TEACHER == profile) {
            Gender tGender = (null != gender && Gender.UNKNOWN != gender) ? gender : Gender.UNKNOWN;
            suffixComponents.add(tGender.name());
        }

        return ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.USER, suffixComponents);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", orgId:").append(orgId)
                .append(", memberId:").append(memberId).append(", firstName:").append(firstName)
                .append(", lastName:").append(lastName).append(", dob:").append(dob)
                .append(", gender:").append(gender).append(", email:").append(email)
                .append(", profile:").append(profile).append(", contactNumber:")
                .append(contactNumber).append(", mappings:").append(mappings)
                .append(", thumbnail:").append(thumbnail).append(", father:").append(father)
                .append(", mother:").append(mother).append(", guardian:").append(guardian)
                .append(", parentEmail:").append(parentEmail).append(", canImpersonate:")
                .append(canImpersonate).append(", status:").append(status).append(", interval:")
                .append(interval).append(", authType:").append(authType).append(", extUserId:")
                .append(extUserId).append(", extraInfo:").append(extraInfo).append(", id:")
                .append(id).append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
