package com.vedantu.organization.pojos.requests.members;

import java.util.Collections;
import java.util.List;

import com.vedantu.organization.models.OrgMember;
import com.vedantu.user.pojos.requests.TestUserDataReq;
import play.data.validation.Constraints.Required;

import com.vedantu.billing.pojos.SaleDetailsInfo;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrgMemberProfile;

public class AddOrgMemberMappingReq extends AbstractAuthCheckReq {

    @Required
    public String           orgId;
    @Required
    public String           targetUserId;
    @Required
    public String           targetOrgMemberId;
    @Required
    public OrgMemberProfile targetProfile;
    @Required
    public String           programId;
    @Required
    public String           centerId;
    @Required
    public List<String>     sectionIds;
    public List<String>     courseIds;
    public int              packageDays;

    public boolean          returnOrgProfileWithCourseInfo;
    public boolean          returnNewlyAddedMapping;

    /**
     * transactionId --> if the sectionIds is paid then we will verify this transactionId and add
     * the mapping accordingly
     **/
    public String           transactionId;

    // below two params will be required if the user has paid through some third party channel (i.e
    // Snapdeal,Amazon,Flipkart etc)
    public String           pointOfSale;
    public String           sellerReferenceNo;

    public SaleDetailsInfo  saleDetailsInfo;

    public AddOrgMemberMappingReq() {

        super();
    }

    public AddOrgMemberMappingReq(String orgId, String targetUserId, String targetOrgMemberId,
            OrgMemberProfile targetProfile, String programId, String centerId,
            List<String> sectionIds, List<String> courseIds, String pointOfSale,
            String sellerReferenceNo, SaleDetailsInfo saleDetailsInfo) {

        super();
        this.orgId = orgId;
        this.targetUserId = targetUserId;
        this.targetOrgMemberId = targetOrgMemberId;
        this.targetProfile = targetProfile;
        this.programId = programId;
        this.centerId = centerId;
        this.sectionIds = sectionIds;
        this.courseIds = courseIds;
        this.pointOfSale = pointOfSale;
        this.sellerReferenceNo = sellerReferenceNo;
        this.saleDetailsInfo = saleDetailsInfo;
    }

    public void createFromTestUserReq(TestUserDataReq req, String programId, String centerId, OrgMember member) {
        this.orgId = req.orgId;
        this.centerId = centerId;
        this.programId = programId;
        this.sectionIds = Collections.singletonList(req.sectionId);
        this.callingUserId = req.adminUserId;
        this.targetOrgMemberId = member._getStringId();
        this.targetUserId = member.userId;
        this.targetProfile = OrgMemberProfile.STUDENT;
        this.userId = member.userId;
    }

}
