package com.lms.user.vedantu.user.pojo.responce;

import com.lms.common.vedantu.enums.AuthType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserAuthRes {
    public String id;
    public String firstName;
    public String lastName;
    public boolean needsTnCAcceptance;
    public String latestTnCVersion;
    public String thumbnail;
    public String acceptedTNCVersion;
    public AuthType authType;
    public String memberId;
}
