package com.lms.user.vedantu.user.pojo;


import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.model.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserInfo  extends ModelBasicInfo {

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

    public UserInfo(User user) {

        super(user.getId().toString(), user.getRecordState());
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
