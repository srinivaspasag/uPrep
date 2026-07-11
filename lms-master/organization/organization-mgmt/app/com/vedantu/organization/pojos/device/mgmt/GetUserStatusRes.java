package com.vedantu.organization.pojos.device.mgmt;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;
import com.vedantu.user.enums.UserStatus;

public class GetUserStatusRes implements IListResponseObj {

    public GetUserStatusRes(OrgMemberExtendedInfo info) {

        super();
        this.memberInfo = info;

        this.statuses = new HashMap<String, UserStatus>();

    }

    public OrgMemberExtendedInfo   memberInfo;
    public Map<String, UserStatus> statuses;

}
