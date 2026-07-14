package com.vedantu.comm.daos;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.comm.models.mongo.ConversationSummary;
import com.vedantu.mongo.MongoManager.NumberUpdate;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class ConversationSummaryDAO extends VedantuBasicDAO<ConversationSummary, ObjectId> {

    static ALogger                             LOGGER   = Logger.of(ConversationSummaryDAO.class);

    public static final ConversationSummaryDAO INSTANCE = new ConversationSummaryDAO();

    public ConversationSummaryDAO() {

        super(ConversationSummary.class);
    }

    /**
     * Upserts conversation summary if doesn't exist and sets state to ACTIVE
     * 
     * @param conversationId
     * @param userId
     * @param sender
     * @param firstMessageId
     * @param participants
     * @param orgId
     * @param mostRecentMessageTime
     * @return olderConversationSummary if conversation is reinitiated by setting state DELETED to
     *         ACTIVE
     */
    public ConversationSummary update(String conversationId, String userId, boolean sender,
            String firstMessageId, int participants, String orgId, long mostRecentMessageTime) {

        Query<ConversationSummary> findQuery = getQuery();
        findQuery.field("conversationId").equal(conversationId);
        findQuery.field("userId").equal(userId);

        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);

        if (!sender) {
            updateOperations.inc("messagesUnread");
            updateOperations.set("status", ConversationStatus.UNREAD);

        } else {
            updateOperations.set("messagesUnread", 0);
            updateOperations.set("status", ConversationStatus.READ);
        }
        updateOperations.set("recordState", VedantuRecordState.ACTIVE);
        updateOperations.inc("messageCount");
        updateOperations.set("mostRecentMessageTime", mostRecentMessageTime);
        updateOperations.set("firstMessageId", firstMessageId);
        updateOperations.set("numOfParticipants", participants);
        if (StringUtils.isNotEmpty(orgId)) {
            updateOperations.set("orgId", orgId);
        }

        // return find and modify and return correct count if
        ConversationSummary updateResults = getDS().findAndModify(findQuery, updateOperations,
                true, true);

        LOGGER.debug("testing upsert and retriving new messages" + updateResults);
        if (updateResults != null) {
            LOGGER.debug(" Updated messageUnread and messageCount");

        }
        return updateResults;

    }

    public ConversationSummary updateIncForNewMessage(String conversationId, String userId,
            boolean sender) {

        Query<ConversationSummary> findQuery = getQuery();
        findQuery.field("conversationId").equal(conversationId);
        findQuery.field("userId").equal(userId);
        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);

        if (!sender) {
            updateOperations.inc("messagesUnread");
            updateOperations.set("status", ConversationStatus.UNREAD);
        }
        updateOperations.inc("messageCount");
        // return find and modify and return correct count if
        ConversationSummary updateResults = getDS()
                .findAndModify(findQuery, updateOperations, true);

        if (updateResults != null) {
            LOGGER.debug(" Updated messageUnread and messageCount");

        }
        return updateResults;

    }

    public ConversationSummary find(String conversationId, String userId) {

        Query<ConversationSummary> findQuery = getQuery();
        findQuery.field("conversationId").equal(conversationId);
        findQuery.field("userId").equal(userId);

        return findQuery.get();

    }

    /**
     * Can not increment message
     * 
     * @param conversationId
     * @param userId
     * @param currentMessageContent
     * @param subject
     * @param currentMessageTime
     * @param currentMessageSenderId
     * @return
     */
    public boolean updateMessageSummary(String conversationId, String userId,
            String currentMessageContent, String currentMessageSubject, long currentMessageTime,
            String currentMessageSenderId, String currentMessageId) {

        LOGGER.debug("Updateing conversation Id " + conversationId + " for userId " + userId
                + " with most reced message time " + currentMessageTime
                + " with recent message sender Id" + currentMessageSenderId);

        Query<ConversationSummary> findQuery = getQuery();
        findQuery.field("conversationId").equal(conversationId);
        findQuery.field("userId").equal(userId);

        findQuery.field("mostRecentMessageTime").lessThanOrEq(currentMessageTime);
        findQuery.field("recordState").equal(VedantuRecordState.ACTIVE);

        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);
        updateOperations.set("content", currentMessageContent);
        if (StringUtils.isNotEmpty(currentMessageSubject)) {
            updateOperations.set("subject", currentMessageSubject);
        }

        updateOperations.set("mostRecentSenderId", currentMessageSenderId);
        updateOperations.set("mostRecentMessageId", currentMessageId);
        updateOperations.set("mostRecentMessageTime", currentMessageTime);

        // TODO we can do increment number of message count and unread message count but
        // as of now I am not but need to be evaluated what happens
        // assumption if this operation fails so I can update only counts as conversationsummary
        // already counted for latest message
        UpdateResults<ConversationSummary> updateResults = this.update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messagesUnread and messageCount");
            return true;
        }
        LOGGER.debug(" failed to updated conversation ");
        return false;
    }

    public List<ConversationSummary> get(String userId, int start, int size, MutableLong totalHits,
            VedantuRecordState state) {

        Query<ConversationSummary> findQuery = getQuery();

        findQuery.field("userId").equal(userId);
        SortOrder sortOrder = SortOrder.DESC;

        findQuery.field("recordState").equal(state);

        String sortOrderBy = null;

        sortOrderBy = "-mostRecentMessageTime";

        totalHits.setValue(findQuery.countAll());
        List<ConversationSummary> summaries = findQuery.offset(start).limit(size)
                .order(sortOrderBy).asList();

        LOGGER.debug(" query for " + findQuery.toString() + " \n summaries " + summaries);
        if (sortOrder == SortOrder.ASC) {
            Collections.reverse(summaries);
        }
        return summaries;

    }

    public boolean incMessageUnread(String conversationSummaryId, int count) {

        return updateMessageUnread(conversationSummaryId, count, NumberUpdate.INCREMENT);
    }

    public boolean decMessageUnread(String conversationSummaryId, int count) {

        return updateMessageUnread(conversationSummaryId, count, NumberUpdate.DECREMENT);
    }

    private boolean
            updateMessageUnread(String conversationSummaryId, int count, NumberUpdate update) {

        Query<ConversationSummary> findQuery = getQuery();
        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);
        findQuery.field(FIELD_ID).equal(new ObjectId(conversationSummaryId));

        if (update == NumberUpdate.DECREMENT) {
            findQuery.field("messagesUnread").greaterThanOrEq(count);
            updateOperations.inc("messagesUnread", new Integer((0 - count)));
        } else {
            updateOperations.inc("messagesUnread", new Integer(count));

        }

        UpdateResults<ConversationSummary> updateResults = this.update(findQuery, updateOperations);

        if (!updateResults.getHadError()) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return true;
        }
        return false;

    }

    public ConversationSummary resetMessageUnread(String conversationSummaryId,
            ConversationStatus status) {

        Query<ConversationSummary> findQuery = getQuery();
        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);
        findQuery.field(FIELD_ID).equal(new ObjectId(conversationSummaryId));
        if (status == ConversationStatus.READ) {
            updateOperations.set("messagesUnread", new Integer(0));

        }
        updateOperations.set("status", status);

        ConversationSummary updateResults = getDS()
                .findAndModify(findQuery, updateOperations, true);

        if (updateResults == null) {
            LOGGER.debug(" failed to update messageUnread and messageCount");
            return null;
        }
        return updateResults;

    }

    public ConversationSummary markDeleted(String summaryId, String userId) {

        Query<ConversationSummary> findQuery = getQuery();
        UpdateOperations<ConversationSummary> updateOperations = getDS().createUpdateOperations(
                ConversationSummary.class);
        findQuery.field("userId").equal((userId));
        findQuery.field("conversationId").equal(summaryId);
        findQuery.field("recordState").equal(VedantuRecordState.ACTIVE);

        updateOperations.set("recordState", VedantuRecordState.DELETED);

        // updateOperations.set("messagesUnread", new Integer(0));
        // updateOperations.set("messageCount", new Integer(0));

        ConversationSummary olderSummary = getDS().findAndModify(findQuery, updateOperations, true);

        if (olderSummary != null) {
            LOGGER.debug(" Updated messageUnread and messageCount");
            return olderSummary;
        }
        return null;
    }
}
