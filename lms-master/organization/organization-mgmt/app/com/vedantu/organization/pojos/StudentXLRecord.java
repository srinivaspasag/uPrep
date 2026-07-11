package com.vedantu.organization.pojos;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberReq;
import com.vedantu.user.enums.Gender;

public class StudentXLRecord {

    public int                      rowNum;
    public String                   memberId;
    public String                   firstName;
    public String                   lastName;
    public String                   year;
    public String                   center;
    public String                   section;
    public String                   email;
    public String                   contactNumber;
    public Gender                   gender;
    public String                   dob;
    public MemberParentInfo         father;
    public MemberParentInfo         mother;
    public MemberParentInfo         guardian;
    public String                   parentEmail;

    public String                   pointOfSale;
    public String                   sellerReferenceNo;

    public List<OrgMemberExtraInfo> extraInfo;

    public StudentXLRecord(int rowNum) {

        this.rowNum = rowNum;
        this.extraInfo = new ArrayList<OrgMemberExtraInfo>();
    }

    public void addMemberExtraInfo(String fieldName, String fieldValue) {

        this.extraInfo.add(new OrgMemberExtraInfo(fieldName, fieldValue));
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{rowNum:").append(rowNum).append(", memberId:").append(memberId)
                .append(", firstName:").append(firstName).append(", lastName:").append(lastName)
                .append(", year:").append(year).append(", center:").append(center)
                .append(", section:").append(section).append(", email:").append(email)
                .append(", contactNumber:").append(contactNumber).append(", gender:")
                .append(gender).append(", dob:").append(dob).append(", father:").append(father)
                .append(", mother:").append(mother).append(", guardian:").append(guardian)
                .append(", parentEmail:").append(parentEmail).append(", pointOfSale:")
                .append(pointOfSale).append(", sellerReferenceNo:").append(sellerReferenceNo)
                .append(", extraInfo:").append(extraInfo).append("}");
        return builder.toString();
    }

    public AddOrgMemberReq toAddOrgMemberReq(String orgId) {

        AddOrgMemberReq addOrgMemberReq = new AddOrgMemberReq(orgId, memberId, firstName, lastName,
                dob, gender, email, OrgMemberProfile.STUDENT, contactNumber);
        addOrgMemberReq.father = father;
        addOrgMemberReq.mother = mother;
        addOrgMemberReq.guardian = guardian;
        addOrgMemberReq.setParentEmail(parentEmail);
        addOrgMemberReq.extraInfo = extraInfo;
        return addOrgMemberReq;
    }

}
