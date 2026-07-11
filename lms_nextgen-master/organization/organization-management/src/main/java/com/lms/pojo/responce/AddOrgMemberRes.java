package com.lms.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddOrgMemberRes extends ModelBasicInfo {

    public String   orgId;
    public String   userId;
    public String   orgMemberId;
    public String   memberId;

    public String   firstName;
    public String   lastName;
    public String   contactNumber;
    public boolean  needsTnCAcceptance;
    public String   latestTnCVersion;
    public String   thumbnail;
    public AuthType authType;
    public boolean  autoAddDemoProgram;
    public boolean  autoAddCampaignProgram;
    public boolean  showSpecialMessage;

    public AddOrgMemberRes(String id, VedantuRecordState recordState, String orgId, String userId) {

        super(id, recordState);
        this.orgId = orgId;
        this.userId = userId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("AddOrgMemberRes [orgId=");
        builder.append(orgId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", id=");
        builder.append(id);
        builder.append(", recordState=");
        builder.append(recordState);
        builder.append("]");
        return builder.toString();
    }

}
