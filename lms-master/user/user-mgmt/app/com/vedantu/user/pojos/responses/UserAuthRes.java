package com.vedantu.user.pojos.responses;

import com.vedantu.commons.enums.AuthType;

public class UserAuthRes {

    public String   id;
    public String   firstName;
    public String   lastName;
    public boolean  needsTnCAcceptance;
    public String   latestTnCVersion;
    public String   thumbnail;
    public String   acceptedTNCVersion;
    public AuthType authType;
    public String   memberId;

}
