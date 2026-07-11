package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationInfo extends ModelExtendedInfo implements
        IListResponseObj {
    public String regId;
    public String userId;

    public NotificationInfo() {

    }

    public NotificationInfo(String regId, String userId) {
        this.regId = regId;
        this.userId = userId;
    }
}
