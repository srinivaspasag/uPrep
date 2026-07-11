package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrgMemberState;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.model.LoginStatus;
import com.lms.user.vedantu.user.pojo.MemberParentInfo;

import java.util.Date;
import java.util.List;

public class OrgMemberExtendedInfo extends ModelExtendedInfo {

    public String userId;
    public String orgId;
    public String memberId;
    public String firstName;
    public String lastName;
    public String dob;
    public Gender gender;
    public String email;
    public OrgMemberProfile profile;
    public String thumbnail;
    public String contactNumber;
    public OrgMemberMappingExtendedInfo mappings;

    // populated for self profile page or CMDS profile page
    public String username;
    public String verifiedEmail;
    public boolean isEmailVerified;
    public boolean isPhoneVerified;
    public boolean isUsernameOrgSpecific;

    // Following fields are applicable only for STUDENT
    public MemberParentInfo father;
    public MemberParentInfo mother;
    public MemberParentInfo guardian;
    public String parentEmail;
    public List<LoginStatus> loginStatus;
    public boolean canImpersonate;
    public long activeFrom;
    public long activeTill;
    public OrgMemberState userState;
    public List<OrgMemberExtraInfo> extraInfo;

    public OrgMemberExtendedInfo(String id, VedantuRecordState recordState, String name,
                                 long timeCreated, long lastUpdated, String userId, String orgId, String memberId,
                                 String firstName, String lastName, String dob, Gender gender, String email,
                                 OrgMemberProfile profile, String thumbnail, String contactNumber,
                                 OrgMemberMappingExtendedInfo mappings, boolean canImpersonate, long activeFrom,
                                 long activeTill) {

        this(id, recordState, name, timeCreated, lastUpdated, userId, orgId, memberId, firstName,
                lastName, dob, gender, email, profile, thumbnail, contactNumber, mappings, null,
                null, null, HardCodedConstants.emptyString, canImpersonate, activeFrom, activeTill);
    }
    public OrgMemberExtendedInfo(String id, VedantuRecordState recordState, String name,
                                 long timeCreated, long lastUpdated, String userId, String orgId, String memberId,
                                 String firstName, String lastName, String dob, Gender gender, String email,
                                 OrgMemberProfile profile, String thumbnail, String contactNumber,
                                 OrgMemberMappingExtendedInfo mappings, MemberParentInfo father,
                                 MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,
                                 boolean canImpersonate, long activeFrom, long activeTill) {

        super(id, recordState, name, timeCreated, lastUpdated);
        long currentTime = new Date().getTime();
        this.userId = userId;
        this.orgId = orgId;
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.profile = profile;
        this.thumbnail = thumbnail;
        this.contactNumber = contactNumber;
        this.mappings = mappings;
        this.father = father;
        this.mother = mother;
        this.guardian = guardian;
        this.parentEmail = parentEmail;
        this.canImpersonate = canImpersonate;
        this.activeFrom = activeFrom;
        this.activeTill = activeTill;
        if (activeFrom < currentTime && activeTill < currentTime && activeTill != -1) {
            this.activeFrom = -1;
            this.activeTill = -1;
        }
    }
    public OrgMemberExtendedInfo(String id, VedantuRecordState recordState, String name,
                                 long timeCreated, long lastUpdated, String userId, String orgId, String memberId,
                                 String firstName, String lastName, String dob, Gender gender, String email,
                                 OrgMemberProfile profile, String thumbnail, String contactNumber,
                                 OrgMemberMappingExtendedInfo mappings, MemberParentInfo father,
                                 MemberParentInfo mother, MemberParentInfo guardian, String parentEmail,List<LoginStatus> loginStatus,
                                 boolean canImpersonate, long activeFrom, long activeTill) {

        super(id, recordState, name, timeCreated, lastUpdated);
        long currentTime = new Date().getTime();
        this.userId = userId;
        this.orgId = orgId;
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.profile = profile;
        this.thumbnail = thumbnail;
        this.contactNumber = contactNumber;
        this.mappings = mappings;
        this.father = father;
        this.mother = mother;
        this.guardian = guardian;
        this.parentEmail = parentEmail;
        this.loginStatus=loginStatus;
        this.canImpersonate = canImpersonate;
        this.activeFrom = activeFrom;
        this.activeTill = activeTill;
        if (activeFrom < currentTime && activeTill < currentTime && activeTill != -1) {
            this.activeFrom = -1;
            this.activeTill = -1;
        }
    }

    public void setUserPublicProfileDetails(String username, String verifiedEmail,
                                            boolean isUsernameOrgSpecific) {

        this.username = username;
        this.verifiedEmail = verifiedEmail;
        this.isUsernameOrgSpecific = isUsernameOrgSpecific;
    }

}
