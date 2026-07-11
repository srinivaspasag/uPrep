package com.vedantu.comm.daos;

import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.BasicDBObject;
import com.vedantu.comm.models.mongo.Conversation;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;

public class ConversationDAO extends VedantuBasicDAO<Conversation, ObjectId> {

    static ALogger                      LOGGER   = Logger.of(ConversationDAO.class);

    public static final ConversationDAO INSTANCE = new ConversationDAO();

    public ConversationDAO() {

        super(Conversation.class);
    }

    public void updateIncForNewMessage(String conversationId) {

        Query<Conversation> findQuery = getDS().createQuery(Conversation.class);
        findQuery.field("conversationId").equal(conversationId);

        UpdateOperations<Conversation> updateOperations = getDS().createUpdateOperations(
                Conversation.class);
        updateOperations.inc("messageCount");
        UpdateResults<Conversation> updateResults = this.update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messageUnread and messageCount");

        }

    }

    public boolean updateRecentMessageInfo(String conversationId, long recentMessageTime,
            String recentMessageId) {

        LOGGER.debug("Updateing conversation Id " + conversationId
                + " with most reced message time " + recentMessageTime + " with recent message Id"
                + recentMessageId);

        Query<Conversation> findQuery = getDS().createQuery(Conversation.class);
        findQuery.field(FIELD_ID).equal(new ObjectId(conversationId));
        findQuery.field("recentMessageTime").lessThanOrEq(recentMessageTime);

        UpdateOperations<Conversation> updateOperations = getDS().createUpdateOperations(
                Conversation.class);

        updateOperations.set("recentMessageId", recentMessageId);
        updateOperations.set("recentMessageTime", recentMessageTime);

        UpdateResults<Conversation> updateResults = this.update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return true;
        }
        LOGGER.error("Update conversations failed for" + conversationId
                + " while updateing most recent message info ");
        return false;

    }

    public Conversation find(String conversationId) {

        Query<Conversation> findQuery = getQuery();
        findQuery.field("conversationId").equal(conversationId);

        return findQuery.get();

    }

    public Conversation getConversation(String conversationId, int start, int size,
            MutableLong totalHits) {

        BasicDBObject findDetailsQuery = new BasicDBObject();
        findDetailsQuery.put(FIELD_ID, new ObjectId(conversationId));
        BasicDBObject fieldsQuery = new BasicDBObject();
        fieldsQuery.put(
                "participants",
                new BasicDBObject(MongoManager.SLICE, Arrays.asList(new Integer(start),
                        new Integer(size))));
        VedantuDBResult<Conversation> recordResults = getInfos(findDetailsQuery, fieldsQuery,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(recordResults.results)) {
            Conversation record = recordResults.results.get(0);

            return record;
        }
        return null;
    }

}
