package com.vedantu.organization.pojos.responses.members;

import com.vedantu.organization.enums.DoubtsForumMode;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;

public class GetOrgMemberProfileRes {

    public OrgMemberExtendedInfo info;
    public String                key;
    public DoubtsForumMode doubtsForumMode;
    public boolean showClassroomConnect;
}
