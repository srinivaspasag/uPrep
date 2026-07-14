package com.vedantu.comm.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.comm.models.mongo.UserMailBoxInfo;
import com.vedantu.mongo.MongoManager.NumberUpdate;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserMailBoxInfoDAO extends VedantuBasicDAO<UserMailBoxInfo, ObjectId> {

    private static final ALogger           LOGGER   = Logger.of(UserMailBoxInfoDAO.class);

    public static final UserMailBoxInfoDAO INSTANCE = new UserMailBoxInfoDAO();

    private UserMailBoxInfoDAO() {

        super(UserMailBoxInfo.class);
        // TODO Auto-generated constructor stub
    }

    /**
     * this will never fail for
     * 
     * @param userId
     * @return
     */
    public UserMailBoxInfo getByUserId(String userId) {

        Query<UserMailBoxInfo> findQuery = getQuery();
        UpdateOperations<UserMailBoxInfo> updateOperations = getDS().createUpdateOperations(
                UserMailBoxInfo.class);
        findQuery.field("userId").equal(userId);

        updateOperations.set("userId", userId);

        UserMailBoxInfo updateResults = getDS().findAndModify(findQuery, updateOperations, false,
                true);

        if (updateResults != null) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return updateResults;
        }
        return null;

    }

    // TODO this is not safe in distributed environment as of now since there is
    // only one comm service its fine
    /**
     * This will do all additions by handling creation in synchronized way
     * 
     */
    private UserMailBoxInfo add(UserMailBoxInfo userMailBoxInfo) {

        UserMailBoxInfo userMailBoxBeingChecked = getByUserId(userMailBoxInfo.userId);
        if (userMailBoxBeingChecked == null) {
            synchronized (userMailBoxInfo.userId.intern()) {
                userMailBoxBeingChecked = getByUserId(userMailBoxInfo.userId);
                if (userMailBoxBeingChecked == null) {

                    save(userMailBoxInfo);

                    return userMailBoxInfo;
                }
            }
        }

        return userMailBoxBeingChecked;
    }

    public boolean incUnreadConversations(String userId, int count) {

        return updateUnreadConversations(userId, count, NumberUpdate.INCREMENT);
    }

    public boolean decUnreadConversations(String userId, int count) {

        return updateUnreadConversations(userId, count, NumberUpdate.DECREMENT);
    }

    private boolean updateUnreadConversations(String userId, int count, NumberUpdate update) {

        Query<UserMailBoxInfo> findQuery = getQuery();
        UpdateOperations<UserMailBoxInfo> updateOperations = getDS().createUpdateOperations(
                UserMailBoxInfo.class);
        findQuery.field("userId").equal(userId);

        if (update == NumberUpdate.DECREMENT) {
            findQuery.field("unreadConversationCount").greaterThanOrEq(count);
            updateOperations.inc("unreadConversationCount", new Integer((0 - count)));
        } else {
            updateOperations.inc("unreadConversationCount", new Integer(count));
        }

        UpdateResults<UserMailBoxInfo> updateResults = this.update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return true;
        }
        return false;

    }

    public boolean deleteConversation(String userId, boolean deletingUnreadConversation,
            boolean sender) {

        Query<UserMailBoxInfo> findQuery = getQuery();
        UpdateOperations<UserMailBoxInfo> updateOperations = getDS().createUpdateOperations(
                UserMailBoxInfo.class);
        findQuery.field("userId").equal(userId);

        if (deletingUnreadConversation) {
            findQuery.field("unreadConversationCount").greaterThanOrEq(1);
            updateOperations.inc("unreadConversationCount", new Integer(-1));
        }

        if (sender) {
            findQuery.field("sentCount").greaterThanOrEq(1);
            updateOperations.inc("sentCount", new Integer(-1));
        }
        findQuery.field("conversationCount").greaterThanOrEq(1);
        updateOperations.inc("conversationCount", new Integer(-1));

        UpdateResults<UserMailBoxInfo> updateResults = getDS().update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return true;
        }
        return false;

    }

    public boolean updateCounts(String userId, boolean sent, boolean unread, boolean total) {

        LOGGER.debug("UserId " + userId + " sent " + sent + " unread " + unread
                + " increment total " + total);
        Query<UserMailBoxInfo> findQuery = getQuery();
        UpdateOperations<UserMailBoxInfo> updateOperations = getDS().createUpdateOperations(
                UserMailBoxInfo.class);
        findQuery.field("userId").equal(userId);

        updateOperations.set("userId", userId); // to ensure update happens;
        if (sent) {
            updateOperations.inc("sentCount");
        }
        if (unread) {
            updateOperations.inc("unreadConversationCount");
        }

        if (total) {
            updateOperations.inc("conversationCount");
        }

        UserMailBoxInfo updateResults = getDS().findAndModify(findQuery, updateOperations, false,
                true);

        if (updateResults != null) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return true;
        }
        return false;

    }

}
