package com.lms.pojos.responce.analytics;

import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetEntityAttemptsStudentsListRes {
    public String memberId;
    public UserInfo user;
    public String testStatus;
    public long startTime;
    public boolean processed;
}
