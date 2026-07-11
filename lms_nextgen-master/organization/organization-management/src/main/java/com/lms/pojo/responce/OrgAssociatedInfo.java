package com.lms.pojo.responce;

import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrgMemberState;
import com.lms.enums.OrganizationStatus;
import com.lms.enums.OrganizationType;
import com.lms.pojo.OrgInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrgAssociatedInfo extends OrgInfo {

    public String           orgMemberId;
    public String           memberId;
    public String           firstName;
    public String           lastName;
    public OrgMemberProfile profile;
    public String           thumbnail;
    public OrgMemberState userState;
    public AuthType authType;
    public boolean          showClassroomConnect;
    public OrgMemberState   orgStudentPageStatus;


    public OrgAssociatedInfo(String id, String name, String fullName, OrganizationType type,
                             Scope scope, OrganizationStatus status, String orgThumbnail, long timeCreated,
                             long lastUpdated, VedantuRecordState recordState, String orgMemberId, String memberId,
                             String firstName, String lastName, OrgMemberProfile profile, String thumbnail,
                             OrgMemberState userState, AuthType authType, String referer, String slug, boolean showClassroomConnect) {

        super(id, name, fullName, type, scope, status, orgThumbnail, timeCreated, lastUpdated,referer,slug,
                recordState);
        this.orgMemberId = orgMemberId;
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profile = profile;
        this.thumbnail = thumbnail;
        this.userState = userState;
        this.authType = authType == null ? AuthType.VEDANTU : authType;
        this.showClassroomConnect = showClassroomConnect;
    }

}
