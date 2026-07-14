package com.vedantu.user.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.pojos.UserEmailUnsubscriptionInfo;
import com.vedantu.user.pojos.UserRestrictedEmailCategory;

@Entity(value = "useremailunsubs", noClassnameStored = true)
@Indexes({ @Index(value = "userId,email", unique = true),
        @Index(value = "userId,email,restrictions.category", unique = true) })
public class UserEmailUnsubscription extends VedantuBaseMongoModel {

    public String                            userId;
    public String                            email;

    public List<UserRestrictedEmailCategory> restrictions;

    @Override
    public ModelBasicInfo toBasicInfo() {

        return toExtendedInfo();
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        List<MailCategory> mailCategoryRestrictions = new ArrayList<MailCategory>();
        if (CollectionUtils.isNotEmpty(restrictions)) {
            for (UserRestrictedEmailCategory restriction : restrictions) {
                mailCategoryRestrictions.add(restriction.category);
            }
        }

        return new UserEmailUnsubscriptionInfo(_getStringId(), recordState, userId, email,
                mailCategoryRestrictions, timeCreated, lastUpdated);
    }

}
