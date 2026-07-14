package com.lms.pojos.responce.analytics;

import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserEntityRankRes {
    public int rank;
    public int AIR;
    public UserInfo user;
    public boolean showAIR;
}
