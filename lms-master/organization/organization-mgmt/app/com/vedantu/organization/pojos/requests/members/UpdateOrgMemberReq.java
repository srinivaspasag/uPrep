package com.vedantu.organization.pojos.requests.members;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.pojos.MemberParentInfo;
import com.vedantu.organization.pojos.OrgMemberExtraInfo;
import com.vedantu.user.enums.Gender;

public class UpdateOrgMemberReq extends AbstractAuthCheckReq {

    public static final String      DOB             = "dob";
    public static final String      CAN_IMPERSONATE = "canImpersonate";
    public static final String      FIRST_NAME      = "firstName";
    public static final String      LAST_NAME       = "lastName";
    public static final String      GENDER          = "gender";
    public static final String      EMAIL           = "email";
    public static final String      PROFILE         = "profile";
    public static final String      FATHER          = "father";
    public static final String      MOTHER          = "mother";
    public static final String      GUARDIAN        = "guardian";
    public static final String      PARENT_EMAIL    = "parentEmail";
    public static final String      CONTACT_NUMBER  = "contactNumber";
    public static final String      EXTRA_INFO      = "extraInfo";

    @Required
    public String                   targetUserId;
    @Required
    public String                   targetOrgMemberId;
    @Required
    public String                   orgId;

    private String                  targetMemberId;
    public String                   firstName;
    public String                   lastName        = StringUtils.EMPTY;
    public String                   dob;
    public Gender                   gender;
    private String                  email           = StringUtils.EMPTY;
    public OrgMemberProfile         profile;
    public String                   contactNumber   = StringUtils.EMPTY;

    // Optional fields applicable only in case of STUDENT
    public MemberParentInfo         father;
    public MemberParentInfo         mother;
    public MemberParentInfo         guardian;
    private String                  parentEmail     = StringUtils.EMPTY;
    private boolean                 canImpersonate  = false;

    public List<OrgMemberExtraInfo> extraInfo       = new ArrayList<OrgMemberExtraInfo>();

    public List<String>             updateList      = new ArrayList<String>();

    public UpdateOrgMemberReq() {

    }

    public UpdateOrgMemberReq(String orgId, String targetMemberId, String firstName,
            String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber) {

        this(orgId, targetMemberId, firstName, lastName, dob, gender, email, profile,
                contactNumber, false);
    }

    public UpdateOrgMemberReq(String orgId, String targetMemberId, String firstName,
            String lastName, String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, boolean canImpersonate) {

        this.orgId = orgId;
        setTargetMemberId(targetMemberId);
        this.firstName = firstName;
        this.lastName = null != lastName ? lastName : StringUtils.EMPTY;
        this.dob = dob;
        this.gender = gender;
        setEmail(email);
        this.profile = profile;
        this.contactNumber = null != contactNumber ? contactNumber : StringUtils.EMPTY;
        this.canImpersonate = canImpersonate;
    }

    public String getTargetMemberId() {

        return targetMemberId;
    }

    public void setTargetMemberId(String targetMemberId) {

        this.targetMemberId = StringUtils.upperCase(targetMemberId);
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = null != email ? StringUtils.lowerCase(email) : StringUtils.EMPTY;
    }

    public String getParentEmail() {

        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {

        this.parentEmail = null != parentEmail ? StringUtils.lowerCase(parentEmail)
                : StringUtils.EMPTY;
    }

    public boolean isCanImpersonate() {

        return canImpersonate;
    }

    public void setCanImpersonate(boolean canImpersonate) {

        this.canImpersonate = canImpersonate;
    }

    public String validate() {

        // TODO following block is commented as it was failing if father.name was "" as UI tries to
        // set empty values for this field

        // for (MemberParentInfo parentInfo : new MemberParentInfo[] { father, mother, guardian }) {
        // if (null != parentInfo ) {
        // String parentInfoValidate = parentInfo.validate();
        // if (StringUtils.isNotEmpty(parentInfoValidate)) {
        // return parentInfoValidate;
        // }
        // }
        // }
        return null;
    }

}
