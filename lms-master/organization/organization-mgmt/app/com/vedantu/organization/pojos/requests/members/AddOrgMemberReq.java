package com.vedantu.organization.pojos.requests.members;

import java.util.List;

import com.vedantu.user.pojos.requests.TestUserDataReq;
import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.pojos.MemberParentInfo;
import com.vedantu.organization.pojos.OrgMemberExtraInfo;
import com.vedantu.user.enums.Gender;

public class AddOrgMemberReq extends AbstractAuthCheckReq {

    @Required
    public String                   orgId;

    // if targetMemberId is not provided and email id is provide than
    // targetMemberId will be system generated
    private String                  targetMemberId;
    @Required
    public String                   firstName;
    public String                   lastName      = StringUtils.EMPTY;
    public String                   dob           = StringUtils.EMPTY;
    public Gender                   gender        = Gender.UNKNOWN;
    private String                  email         = StringUtils.EMPTY;
    @Required
    public OrgMemberProfile         profile;
    public String                   countryCode;
    public String                   contactNumber = StringUtils.EMPTY;

    // Optional fields applicable only in case of STUDENT
    public MemberParentInfo         father;
    public MemberParentInfo         mother;
    public MemberParentInfo         guardian;
    private String                  parentEmail   = StringUtils.EMPTY;

    public boolean                  useEmailAsUsername;
    public boolean                  usePhoneAsUsername;
    public boolean                  isOTPsignup;
    public String                   twitterHandle;

    public String                   extUserId     = StringUtils.EMPTY;
    // public boolean canImpersonate;

    // password should only be used in case of direct user signup
    public String                   password;
    public List<OrgMemberExtraInfo> extraInfo;
    public String                   referrerCode;
    public String                   campaignCode;
    public String                   progType;
    public String                   campaignAddProgram = StringUtils.EMPTY;
    public boolean                  isNewUser;
    public boolean                  autoAddDemoProgram;
    public boolean                  autoAddCampaignProgram;

    public boolean                  isValidPhone;

    public AddOrgMemberReq() {

    }

    public AddOrgMemberReq(String orgId, String targetMemberId, String firstName, String lastName,
            String dob, Gender gender, String email, OrgMemberProfile profile, String contactNumber) {

        this(orgId, targetMemberId, firstName, lastName, dob, gender, email, profile,
                contactNumber, false);
    }

    public AddOrgMemberReq(String orgId, String targetMemberId, String firstName, String lastName,
            String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, boolean canImpersonate) {

        this(orgId, targetMemberId, firstName, lastName, dob, gender, email, profile,
                contactNumber, canImpersonate, null);
    }

    public AddOrgMemberReq(String orgId, String targetMemberId, String firstName, String lastName,
            String dob, Gender gender, String email, OrgMemberProfile profile,
            String contactNumber, boolean canImpersonate, String extUserId) {

        this.orgId = orgId;
        setTargetMemberId(targetMemberId);
        this.firstName = firstName;
        this.lastName = null != lastName ? lastName : StringUtils.EMPTY;
        this.dob = dob;
        this.gender = gender;
        setEmail(email);
        this.profile = profile;
        this.contactNumber = null != contactNumber ? contactNumber : StringUtils.EMPTY;
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

    public String validate() {

        // TODO following block is commented as it was failing if father.name was "" as UI tries to
        // set empty values for this field

        // for (MemberParentInfo parentInfo : new MemberParentInfo[] { father,
        // mother, guardian }) {
        // if (null != parentInfo ) {
        // String parentInfoValidate = parentInfo.validate();
        // if (StringUtils.isNotEmpty(parentInfoValidate)) {
        // return parentInfoValidate;
        // }
        // }
        // }
        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{orgId:").append(orgId).append(", targetMemberId:").append(targetMemberId)
                .append(", firstName:").append(firstName).append(", lastName:").append(lastName)
                .append(", dob:").append(dob).append(", gender:").append(gender).append(", email:")
                .append(email).append(", profile:").append(profile).append(", contactNumber:")
                .append(contactNumber).append(", father:").append(father).append(", mother:")
                .append(mother).append(", guardian:").append(guardian).append(", parentEmail:")
                .append(parentEmail).append(", useEmailAsUsername:").append(useEmailAsUsername)
                .append(", twitterHandle:").append(twitterHandle).append(", extUserId:")
                .append(extUserId).append(", password:").append(password).append(", extraInfo:")
                .append(extraInfo).append(", callingUserId:").append(callingUserId)
                .append(", userId:").append(userId).append(", callingApp:").append(callingApp)
                .append(", callingAppId:").append(callingAppId).append("}");
        return builder.toString();
    }

    public void fromTestDataReq(TestUserDataReq request) {
        this.orgId = request.orgId;
        this.profile = OrgMemberProfile.STUDENT;
        this.email = request.email;
        this.firstName = request.name;
        this.lastName = request.surname;
        this.contactNumber = request.studentsMobile;
        this.setTargetMemberId(request.memberId);
        this.father = new MemberParentInfo();
        this.father.contactNumber = request.parentsMobile;
        this.father.name = request.fatherName;
        this.mother = new MemberParentInfo();
        this.mother.name = request.motherName;
        this.useEmailAsUsername = true;
        this.password = request.email.split("@")[0];
    }
}
