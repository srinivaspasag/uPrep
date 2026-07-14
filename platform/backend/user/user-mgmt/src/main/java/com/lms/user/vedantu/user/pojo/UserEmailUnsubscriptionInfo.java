package com.lms.user.vedantu.user.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Setter;

import java.util.List;

@Setter

public class UserEmailUnsubscriptionInfo  extends ModelExtendedInfo {

    public String             userId;
    public String             email;

    public List<MailCategory> restrictions;

    public UserEmailUnsubscriptionInfo(String id, VedantuRecordState recordState, String userId,
                                       String email, List<MailCategory> restrictions, long timeCreated, long lastUpdated) {

        super(id, recordState, null, timeCreated, lastUpdated);
        this.userId = userId;
        this.email = email;
        this.restrictions = restrictions;
    }

}