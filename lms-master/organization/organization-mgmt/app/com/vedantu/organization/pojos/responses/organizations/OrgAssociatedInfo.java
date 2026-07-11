package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.enums.OrgMemberState;
import com.vedantu.organization.enums.OrganizationStatus;
import com.vedantu.organization.enums.OrganizationType;

public class OrgAssociatedInfo extends OrgInfo {

    public String           orgMemberId;
    public String           memberId;
    public String           firstName;
    public String           lastName;
    public OrgMemberProfile profile;
    public String           thumbnail;
    public OrgMemberState   userState;
    public AuthType         authType;
    public boolean          showClassroomConnect;
    public OrgMemberState   orgStudentPageStatus;


    public OrgAssociatedInfo(String id, String name, String fullName, OrganizationType type,
            Scope scope, OrganizationStatus status, String orgThumbnail, long timeCreated,
            long lastUpdated, VedantuRecordState recordState, String orgMemberId, String memberId,
            String firstName, String lastName, OrgMemberProfile profile, String thumbnail,
            OrgMemberState userState, AuthType authType,String referer,String slug,boolean showClassroomConnect) {

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
