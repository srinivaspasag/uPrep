package com.lms.pojo.responce;

import com.lms.enums.OrganizationStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApproveOrgRes {

    public String orgId;
    public boolean isNewUserAdded;
    public String adminUserId;
    public String adminPassword;
    public boolean isNewOrgMemberAdded;
    public String adminOrgMemberId;
    public OrganizationStatus status;

}
