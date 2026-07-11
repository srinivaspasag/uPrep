package com.vedantu.user.pojos;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class UserInfo extends ModelBasicInfo {

    public String   firstName;
    public String   lastName;
    public String   thumbnail;
    public AuthType authType = AuthType.VEDANTU;

    public UserInfo(String id, VedantuRecordState recordState) {

        super(id, recordState);
    }

    public UserInfo(String id, String firstName, String lastName, String thumbnail,
            VedantuRecordState recordState) {

        super(id, recordState);
        this.firstName = firstName;
        this.lastName = lastName;
        this.thumbnail = thumbnail;
     
    }

    @Override
    public String toString() {

        return "UserInfo [firstName=" + firstName + ", lastName=" + lastName + ", thumbnail="
                + thumbnail + ", id=" + id + ", recordState=" + recordState + "]";
    }

}
