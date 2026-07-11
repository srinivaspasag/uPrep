package com.lms.models;

import com.lms.billing.pojo.SaleDetailsInfo;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.OrgMemberProfile;
import com.lms.user.vedantu.user.pojo.TestUserDataReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;
@Setter
@Getter
public class AddOrgMemberMappingReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "targetUserId should not be null")
    public String targetUserId;
    @NotBlank(message = "targetOrgMemberId should not be null")
    public String targetOrgMemberId;
   // @NotBlank(message = "targetProfile should not be null")
    public OrgMemberProfile targetProfile;
    @NotBlank(message = "programId should not be null")
    public String programId;
    @NotBlank(message = "centerId should not be null")
    public String centerId;
    //@NotBlank(message = "orgId should not be null")
    public List<String> sectionIds;
    public List<String> courseIds;
    public int packageDays;

    public boolean returnOrgProfileWithCourseInfo;
    public boolean returnNewlyAddedMapping;

    /**
     * transactionId --> if the sectionIds is paid then we will verify this transactionId and add
     * the mapping accordingly
     **/
    public String transactionId;

    // below two params will be required if the user has paid through some third party channel (i.e
    // Snapdeal,Amazon,Flipkart etc)
    public String pointOfSale;
    public String sellerReferenceNo;

    public SaleDetailsInfo saleDetailsInfo;

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
        this.orgId = req.getOrgId();
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