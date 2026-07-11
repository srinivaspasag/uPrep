package com.lms.user.vedantu.user.pojo.responce;

import com.lms.user.vedantu.user.pojo.UserEmailUnsubscriptionInfo;
import com.lms.user.vedantu.user.pojo.UserExtendedInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUserSelfFullProfileRes {
    public UserExtendedInfo info;
    public UserEmailUnsubscriptionInfo unsubscribeInfo;
}
