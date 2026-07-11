package com.lms.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojo.OrgMemberExtendedInfo;
import com.lms.user.vedantu.user.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter

public class GetUserStatusRes implements IListResponseObj {
    public OrgMemberExtendedInfo memberInfo;
    public Map<String, UserStatus> statuses;

    public GetUserStatusRes(OrgMemberExtendedInfo info) {

        super();
        this.memberInfo = info;

        this.statuses = new HashMap<String, UserStatus>();

    }
}
