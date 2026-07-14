package com.lms.models;


import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.Interval;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.entity.storage.*;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrgMemberState;
import com.lms.pojo.*;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.model.LoginStatus;
import com.lms.user.vedantu.user.pojo.MemberParentInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.*;

@Document(collection = "orgmembers")
@CompoundIndexes({
        @CompoundIndex(name = "orgId, memberId", unique = true),
        @CompoundIndex(name = "orgId, userId", unique = true),
        @CompoundIndex(name = "orgId, profile, mappings.programId, mappings.centerId, mappings.sectionId"),
        @CompoundIndex(name = "orgId, profile, mappings.courseIds")})
@Setter
@Getter

public class OrgMember extends VedantuBaseMongoModel {

    public transient static final String FIELD_DOB = "dob";
    @Autowired
    private UserProfilePicEntityFileStorage userStorage;
    public transient static final String FIELD_FIRST_NAME = "firstName";
    public transient static final String FIELD_LAST_NAME = "lastName";
    public transient static final String FIELD_EMAIL = "email";
    public transient static final String FIELD_PROFILE = "profile";
    public transient static final String FIELD_CONTACT_NUMBER = "contactNumber";
    public transient static final String FIELD_MAPPINGS = "thumbnail";
    public transient static final String FIELD_FATHER = "father";
    public transient static final String FIELD_MOTHER = "mother";
    public transient static final String FIELD_GUARDIAN = "guardian";
    public transient static final String FIELD_PARENT_EMAIL = "parentEmail";
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
    public Gender gender;
    public String                        email;
    public OrgMemberProfile profile;

    @Indexed
    public String                        contactNumber;
    public String                        countryCode;
    public Set<OrgMemberMappingInfo> mappings;
    public List<OrgMemberMappingInfo> expiredMappings;
    public String                        thumbnail;

    // Following fields are applicable only for STUDENT
    public MemberParentInfo father;
    public MemberParentInfo              mother;
    public MemberParentInfo              guardian;
    public String                        parentEmail;
    public boolean                       canImpersonate        = false;
    public List<LoginStatus>             status;
    public Interval interval;

    public AuthType authType;
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
            if (programId.equals(mInfo.getProgramId())) {
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
            if (mapping.getCenterId().equals(tMapping.getCenterId())&&mapping.getSectionId().equals(tMapping.getSectionId())&&mapping.getProgramId().equals(tMapping.getProgramId())) {
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
                    gender, email, profile,getThumbnail(), contactNumber, tMappings, father,
                    mother, guardian, parentEmail,status, this.canImpersonate, this.interval.getFrom(),
                    this.interval.getTill());
        } else {
            extendedInfo = new OrgMemberExtendedInfo(_getStringId(), recordState, firstName,
                    timeCreated, lastUpdated, userId, orgId, memberId, firstName, lastName, dob,
                    gender, email, profile, getThumbnail(), contactNumber, tMappings,
                    this.canImpersonate, this.interval.getFrom(), this.interval.getTill());

        }
        //extendedInfo.loginStatus = this.status;
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
        basicInfo.setMappings(tMappings);

        return basicInfo;
    }

    public String _getThumbnailUrl() {

        if (!StringUtils.isEmpty(thumbnail)) {
            return getEntityThumbnail(EntityType.USER, thumbnail);
        }

        List<String> suffixComponents = new ArrayList<String>();
        suffixComponents.add(profile.name());

        if (OrgMemberProfile.STUDENT == profile || OrgMemberProfile.TEACHER == profile) {
            Gender tGender = (null != gender && Gender.UNKNOWN != gender) ? gender : Gender.UNKNOWN;
            suffixComponents.add(tGender.name());
        }

        return ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.USER, suffixComponents);
    }

    public String getEntityThumbnail(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.SMALL);
    }

    public String getEntityImageURL(EntityType entityType, String uid, ImageSize size) {
        AbstractEntityFileStorage abstractEntityFileStorage = new AbstractEntityFileStorage();
        abstractEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
        IEntityFileStorage fileEntityStorage = userStorage;


        return ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL + fileEntityStorage.computeDisplayUrlComponent(uid, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, size);
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
