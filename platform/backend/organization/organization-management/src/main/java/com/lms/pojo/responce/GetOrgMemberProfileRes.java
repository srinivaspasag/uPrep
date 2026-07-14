package com.lms.pojo.responce;

import com.lms.enums.DoubtsForumMode;
import com.lms.pojo.OrgMemberExtendedInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetOrgMemberProfileRes {
    public OrgMemberExtendedInfo info;
    public String                key;
    public DoubtsForumMode doubtsForumMode;
    public boolean showClassroomConnect;
}
