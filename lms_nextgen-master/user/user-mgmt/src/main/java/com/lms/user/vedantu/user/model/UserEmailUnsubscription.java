package com.lms.user.vedantu.user.model;

import com.lms.user.vedantu.user.pojo.UserEmailUnsubscriptionInfo;
import com.lms.user.vedantu.user.pojo.UserRestrictedEmailCategory;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(collection = "useremailunsubs")
@CompoundIndexes({
        @CompoundIndex(name = "userId_email", def = "{'userId' : 1, 'email': 1}"),
        @CompoundIndex(name = "userId_email_restrictions_category", def = "{'userId' : 1, 'email': 1,restrictions:1,category:1}")

})
public class UserEmailUnsubscription extends VedantuBaseMongoModel {

    public String                            userId;
    public String                            email;

    public List<UserRestrictedEmailCategory> restrictions;



    public UserEmailUnsubscription(String userId, String email, List<UserRestrictedEmailCategory> restrictions) {
        this.userId = userId;
        this.email = email;
        this.restrictions = restrictions;
    }

    public UserEmailUnsubscription() {

    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        return toExtendedInfo();
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        List<MailCategory> mailCategoryRestrictions = new ArrayList<MailCategory>();
        if (restrictions.isEmpty()) {
            for (UserRestrictedEmailCategory restriction : restrictions) {
                mailCategoryRestrictions.add(restriction.category);
            }
        }

        return new UserEmailUnsubscriptionInfo(_getStringId(), recordState, userId, email,
                mailCategoryRestrictions, timeCreated, lastUpdated);
    }



}
