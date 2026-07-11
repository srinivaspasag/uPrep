package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.enums.OrgMemberProfile;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.pojo.MemberParentInfo;
import com.lms.user.vedantu.user.pojo.TestUserDataReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class AddOrgMemberReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String                   orgId;

    // if targetMemberId is not provided and email id is provide than
    // targetMemberId will be system generated
    private String                  targetMemberId;
    @NotBlank(message = "firstName should not be null")
    public String                   firstName;
    public String                   lastName      = HardCodedConstants.emptyString;
    public String                   dob           = HardCodedConstants.emptyString;
    public Gender gender        = Gender.UNKNOWN;
    private String                  email         = HardCodedConstants.emptyString;
   // @NotBlank(message = "OrgMemberProfile should not be null")
    public OrgMemberProfile profile;
    public String                   countryCode;
    public String                   contactNumber = HardCodedConstants.emptyString;

    // Optional fields applicable only in case of STUDENT
    public MemberParentInfo father;
    public MemberParentInfo         mother;
    public MemberParentInfo         guardian;
    private String                  parentEmail   = HardCodedConstants.emptyString;

    public boolean                  useEmailAsUsername;
    public boolean                  usePhoneAsUsername;
    public boolean                  isOTPsignup;
    public String                   twitterHandle;

    public String                   extUserId     = HardCodedConstants.emptyString;
    // public boolean canImpersonate;

    // password should only be used in case of direct user signup
    public String                   password;
    public List<OrgMemberExtraInfo> extraInfo;
    public String                   referrerCode;
    public String                   campaignCode;
    public String                   progType;
    public String                   campaignAddProgram = HardCodedConstants.emptyString;
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
        this.lastName = null != lastName ? lastName : HardCodedConstants.emptyString;
        this.dob = dob;
        this.gender = gender;
        setEmail(email);
        this.profile = profile;
        this.contactNumber = null != contactNumber ? contactNumber : HardCodedConstants.emptyString;
    }

    public String getTargetMemberId() {

        return targetMemberId;
    }

    public void setTargetMemberId(String targetMemberId) {

        this.targetMemberId = targetMemberId.toLowerCase();
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = null != email ? email.toLowerCase() :HardCodedConstants.emptyString;
    }

    public String getParentEmail() {

        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {

        this.parentEmail = null != parentEmail ? parentEmail.toLowerCase(): HardCodedConstants.emptyString;
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