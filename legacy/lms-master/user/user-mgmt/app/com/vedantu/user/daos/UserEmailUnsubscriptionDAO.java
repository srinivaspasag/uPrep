package com.vedantu.user.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.models.UserEmailUnsubscription;
import com.vedantu.user.pojos.UserRestrictedEmailCategory;

public class UserEmailUnsubscriptionDAO extends VedantuBasicDAO<UserEmailUnsubscription, ObjectId> {

    private static final ALogger                   LOGGER      = Logger.of(UserEmailUnsubscriptionDAO.class);

    public static final UserEmailUnsubscriptionDAO INSTANCE    = new UserEmailUnsubscriptionDAO();

    public static final String                     UNKNOWN_DOB = "1970-01-01";

    private UserEmailUnsubscriptionDAO() {

        super(UserEmailUnsubscription.class);
    }

    public void restrictEmail(String userId, String targetUserId, String email, String reason,
            MailCategory category) throws VedantuException {

        UpdateOperations<UserEmailUnsubscription> updateOps = getDS().createUpdateOperations(
                this.entityClazz);
        UserRestrictedEmailCategory emailCategory = new UserRestrictedEmailCategory(category,
                userId, reason);
        updateOps.add("restrictions", emailCategory);
        Query<UserEmailUnsubscription> findQuery = getDS().createQuery(this.entityClazz);
        findQuery = findQuery.filter(ConstantsGlobal.USER_ID, targetUserId);
        findQuery.field("email").equal(email);
//        findQuery.field("restrictions.category").notIn(Arrays.asList(category));
        findQuery.field("recordState").equal(VedantuRecordState.ACTIVE);
        UserEmailUnsubscription unsubscription = getDS().findAndModify(findQuery, updateOps, false,
                true);

        if (unsubscription == null) {
            throw new VedantuException(VedantuErrorCode.EMAIL_UNSUBSCRIPTION_FAILED);
        }
        
        

    }

    public boolean isEmailAllowed(String userId, String email, MailCategory category) {

        if (category == null || category == MailCategory.UNKNOWN) {
            return true;
        }

        Query<UserEmailUnsubscription> findQuery = getDS().createQuery(this.entityClazz);
        findQuery = findQuery.filter(ConstantsGlobal.USER_ID, userId);
        findQuery.field("email").equal(email);
        findQuery.field("recordState").equal(VedantuRecordState.ACTIVE);

        findQuery.field("restrictions.category").in(Arrays.asList(MailCategory.ALL, category));
        UserEmailUnsubscription unsubcription = findQuery.get();
        return (unsubcription == null);
    }

    public boolean allowEmails(String userId, String email, MailCategory category)
            throws VedantuException {

        if (category == null || category == MailCategory.UNKNOWN) {
            return true;
        }

        boolean all = (category == MailCategory.ALL);
        LOGGER.debug(" Category " + category);

        Query<UserEmailUnsubscription> findQuery = getDS().createQuery(this.entityClazz);
        findQuery = findQuery.filter(ConstantsGlobal.USER_ID, userId);
        findQuery.field("email").equal(email);

        if (all) {
            getDS().findAndDelete(findQuery);
        } else {

            UserEmailUnsubscription emailUnsubscriptions = findBy(userId, email);
            if (emailUnsubscriptions != null) {
                LOGGER.debug(" Email subscription found");
                if (CollectionUtils.isNotEmpty(emailUnsubscriptions.restrictions)) {
                    List<UserRestrictedEmailCategory> list = new ArrayList<UserRestrictedEmailCategory>();
                    for (UserRestrictedEmailCategory restrictedEmailCategory : emailUnsubscriptions.restrictions) {
                        LOGGER.debug(" Email subscription found " + restrictedEmailCategory);
                        if (restrictedEmailCategory.category != category) {
                            list.add(restrictedEmailCategory);
                        }
                    }
                    if (CollectionUtils.isEmpty(list)) {
                        getDS().findAndDelete(findQuery);
                    } else {
                        emailUnsubscriptions.restrictions = list;
                        UserEmailUnsubscriptionDAO.INSTANCE.save(emailUnsubscriptions);

                    }
                }

            }
        }

        return true;
    }

    public UserEmailUnsubscription findBy(String userId, String email) {

        Query<UserEmailUnsubscription> findQuery = getQuery();
        findQuery = findQuery.filter(ConstantsGlobal.USER_ID, userId);
        findQuery.field("email").equal(email);
        LOGGER.debug(findQuery.toString());
        return findQuery.get();
    }

}
