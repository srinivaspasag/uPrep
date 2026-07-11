package com.lms.user.vedantu.user.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TnCAcceptance {
    public boolean agrees;
    public String  version;
    public long    agreementTime;
    public String  acceptedBy;    // user who has accepted tnc in case of user accpting

    public TnCAcceptance() {

        super();
    }

    public TnCAcceptance(boolean agrees, String version, long agreementTime) {

        this(agrees, version, agreementTime, null);

    }

    public TnCAcceptance(boolean agrees, String version, long agreementTime, String acceptedBy) {

        super();
        this.agrees = agrees;
        this.version = version;
        this.agreementTime = agreementTime;
        this.acceptedBy = acceptedBy;
    }

}
