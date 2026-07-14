package com.vedantu.user.pojos;

import java.util.List;

import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuRecordState;

public class UserEmailUnsubscriptionInfo extends ModelExtendedInfo {

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
