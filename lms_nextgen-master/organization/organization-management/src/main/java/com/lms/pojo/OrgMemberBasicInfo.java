package com.lms.pojo;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.OrgMemberState;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrgMemberBasicInfo extends UserInfo {

    public String                       userId;        // we shall use the id field for user id
    public String                       orgId;
    public String                       memberId;

    public OrgMemberProfile             profile;
    public String                       contactNumber;
    public OrgMemberMappingExtendedInfo mappings;      // added by Shankar
    public boolean                      canImpersonate;
    public OrgMemberState state;

    public OrgMemberBasicInfo(String id, String userId, String orgId, String memberId,
                              String firstName, String lastName, OrgMemberProfile profile, String thumbnail,
                              String contactNumber, VedantuRecordState state, boolean canImpersonate) {

        super(id, firstName, lastName, thumbnail, state);
        this.userId = userId;
        this.orgId = orgId;
        this.memberId = memberId;
        this.profile = profile;
        this.contactNumber = contactNumber;
        this.canImpersonate = canImpersonate;
    }

    @Override
    public String toString() {

        return "OrgMemberBasicInfo [userId=" + userId + ", orgId=" + orgId + ", memberId="
                + memberId + ", profile=" + profile + ", contactNumber=" + contactNumber
                + ", mappings=" + mappings + " canImpersonate=" + canImpersonate + ", toString()="
                 + "]";
    }
}
