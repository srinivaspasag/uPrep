package com.lms.pojo.request;

import com.lms.enums.OrgMemberProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetOrgMembersReq extends AbstractOrgListReq {

    public OrgMemberProfile targetProfile;
    public List<OrgMemberProfile> excludeProfiles;
    public String programId;
    public String centerId;
    public String sectionId;
    public String courseId;
    public String query;
    public List<String> excludes;
    public Boolean canImpersonate;

}
