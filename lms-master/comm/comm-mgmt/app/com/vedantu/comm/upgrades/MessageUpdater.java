package com.vedantu.comm.upgrades;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.comm.daos.ConversationDAO;
import com.vedantu.comm.daos.ConversationSummaryDAO;
import com.vedantu.comm.models.hbase.messages.Conversation;
import com.vedantu.comm.models.hbase.messages.ConversationSummary;
import com.vedantu.comm.models.hbase.messages.Message;
import com.vedantu.comm.models.hbase.messages.MessageSummary;
import com.vedantu.comm.pojos.AddedMember;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.comm.utils.MessageUtil;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;

public class MessageUpdater {

    public static int            BATCH_COUNT = 3;

    private static final ALogger LOGGER      = Logger.of(MessageUpdater.class);

    public static void migrateUserConversations() throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);

        long currentTimeStamp = System.currentTimeMillis() - 365 * 24 * 3600 * 1000;
        final long timeStamp = System.currentTimeMillis();
        long millsecondsIn24Hrs = 24 * 3600 * 1000;
        String excludeRowId = null;
        while (currentTimeStamp < timeStamp) {

            List<ConversationSummary> hbaseConversations = userConversationWrapper.getData(
                    excludeRowId, "conversations", "data", BATCH_COUNT);

            com.vedantu.comm.models.mongo.ConversationSummary mongoConversationSummary = null;
            if (CollectionUtils.isEmpty(hbaseConversations)) {
                LOGGER.debug("No hbase conversations found");
                currentTimeStamp = currentTimeStamp + millsecondsIn24Hrs;
                continue;
            }

            for (ConversationSummary hbaseConversationSummary : hbaseConversations) {

                String userConversationId = hbaseConversationSummary.getKey();
                String[] userConversationSpliited = userConversationId.split("_");

                mongoConversationSummary = ConversationSummaryDAO.INSTANCE.find(
                        hbaseConversationSummary.conversationId, userConversationSpliited[0]);

                // if (mongoConversationSummary != null
                // && mongoConversationSummary.mostRecentMessageTime >
                // hbaseConversationSummary.mostRecentMessageTiming) {
                // continue;
                // }
                if (mongoConversationSummary == null) {
                    mongoConversationSummary = new com.vedantu.comm.models.mongo.ConversationSummary();
                    mongoConversationSummary.timeCreated = hbaseConversationSummary.getTimestamp();
                }

                com.vedantu.comm.models.mongo.Conversation mongConversation = ConversationDAO.INSTANCE
                        .find(hbaseConversationSummary.conversationId);
                if (mongConversation == null) {
                    LOGGER.debug("No mongo conversation found of id "
                            + hbaseConversationSummary.conversationId);
                    continue;

                }
                mongoConversationSummary.conversationId = mongConversation._getStringId();
                mongoConversationSummary.userId = userConversationSpliited[0];
                mongoConversationSummary.orgId = hbaseConversationSummary.orgId;
                mongoConversationSummary.numOfParticipants = hbaseConversationSummary.numOfParticipants;
                mongoConversationSummary.subject = hbaseConversationSummary.subject;
                mongoConversationSummary.content = hbaseConversationSummary.content;
                mongoConversationSummary.status = hbaseConversationSummary.getStatus();

                mongoConversationSummary.firstMessageId = hbaseConversationSummary.firstMessageId;
                mongoConversationSummary.lastUpdated = hbaseConversationSummary.getTimestamp();
                mongoConversationSummary.mostRecentMessageTime = hbaseConversationSummary.mostRecentMessageTiming;
                mongoConversationSummary.mostRecentSenderId = hbaseConversationSummary.mostRecentSender.id;

                mongoConversationSummary.messagesUnread = (int) hbaseConversationSummary.messagesUnread;
                mongoConversationSummary.messageCount = (int) hbaseConversationSummary.messageCount;
                mongoConversationSummary.numOfParticipants = hbaseConversationSummary.numOfParticipants;

                ConversationSummaryDAO.INSTANCE.save(mongoConversationSummary);

                currentTimeStamp = hbaseConversationSummary.getTimestamp() > currentTimeStamp ? hbaseConversationSummary
                        .getTimestamp() : currentTimeStamp;

                excludeRowId = hbaseConversationSummary.getUserConversationId();
                LOGGER.debug("Conversation id " + excludeRowId);
            }

        }

        migrateUserMessages();
    }

    public static void migrateUserMessages() throws VedantuException {

        HbaseTableWrapper<Message> newMessageWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES_V2),
                Message.class);

        HbaseTableWrapper<Message> oldMessageWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES),
                Message.class);

        HbaseTableWrapper<MessageSummary> newUserMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2), MessageSummary.class);

        HbaseTableWrapper<MessageSummary> oldUserMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);

        // String oldUserMessageId = MessageManager.getUserConversationId(new SrcEntity(
        // EntityType.USER, userId), oldConversationId);

        long currentTimeStamp = System.currentTimeMillis() - 365 * 24 * 3600 * 1000;
        final long timeStamp = System.currentTimeMillis();
        long millsecondsIn24Hrs = 24 * 3600 * 1000;
        String excludeRowId = null;

        while (currentTimeStamp < timeStamp) {

            List<MessageSummary> hbaseUserMessages = oldUserMessageWrapper.getData(excludeRowId,
                    "messages", "data", BATCH_COUNT);

            if (CollectionUtils.isEmpty(hbaseUserMessages)) {
                LOGGER.debug("No hbase conversations found");
                currentTimeStamp = currentTimeStamp + millsecondsIn24Hrs;
                continue;
            }

            for (MessageSummary hbaseUserMessage : hbaseUserMessages) {

                excludeRowId = hbaseUserMessage.getKey();

                LOGGER.debug("User Message " + excludeRowId);
                String[] userConversationSpliited = excludeRowId.split("_");
                LOGGER.debug("Splitted Message " + userConversationSpliited);
                com.vedantu.comm.models.mongo.Conversation mongConversation = ConversationDAO.INSTANCE
                        .find(userConversationSpliited[1]);
                if (mongConversation == null) {
                    LOGGER.debug("Not conversation found for " + userConversationSpliited[1]);
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED,
                            "FAiled as not conversation created");
                }

                Message hbaseMessage = newMessageWrapper.getExact(hbaseUserMessage.messageId,
                        "messages", "data");

                hbaseUserMessage.userId = userConversationSpliited[0];
                hbaseUserMessage.conversationId = mongConversation._getStringId();
                hbaseUserMessage.setSentTime(hbaseMessage.getSentOnTimestamp());
                hbaseUserMessage.setReceivedTime(hbaseMessage.getSentOnTimestamp());
                hbaseUserMessage.setTimestamp(hbaseMessage.getSentOnTimestamp());
                hbaseUserMessage.userMessageId= hbaseUserMessage.getNewKey();
               

                ConversationSummaryDAO.INSTANCE.updateMessageSummary(
                        mongConversation._getStringId(), hbaseUserMessage.userId,
                        hbaseUserMessage.content, hbaseMessage.subject,
                        hbaseUserMessage.getSentTime(), hbaseUserMessage.sender.id,
                        hbaseUserMessage.getMessageId());

                newUserMessageWrapper.addData(hbaseUserMessage.getNewKey(), "messages", "data",
                        hbaseUserMessage);
                //
                // currentTimeStamp = hbaseUserMessage.getTimestamp() > currentTimeStamp ?
                // hbaseUserMessage
                // .getTimestamp() : currentTimeStamp;

                LOGGER.debug("hbase user message id id " + excludeRowId);
            }

        }

    }

    public static void migrateConversations() throws IOException, VedantuException {

        HbaseTableWrapper<Conversation> oldConversationWrapper = new HbaseTableWrapper<Conversation>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_CONVERSATION),
                Conversation.class);
        HbaseTableWrapper<Message> oldMessageWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES),
                Message.class);

        long currentTimeStamp = System.currentTimeMillis() - 365 * 24 * 3600 * 1000;
        long millsecondsIn24Hrs = 24 * 3600 * 1000;
        final long timeStamp = System.currentTimeMillis();
        String excludeRowId = null;
        while (currentTimeStamp < timeStamp) {

            List<Conversation> hbaseConversations = oldConversationWrapper.getData(excludeRowId,
                    "conversations", "data", BATCH_COUNT);
            if (CollectionUtils.isEmpty(hbaseConversations)) {
                LOGGER.debug("No hbase conversations found");
                currentTimeStamp = currentTimeStamp + millsecondsIn24Hrs;
                continue;
            }

            com.vedantu.comm.models.mongo.Conversation mongoConversation = null;
            for (Conversation conversation : hbaseConversations) {
                LOGGER.debug("conversation " + conversation);
                mongoConversation = ConversationDAO.INSTANCE.find(conversation.getConversationId());
                if (mongoConversation == null) {
                    LOGGER.debug("mongo record not found for conversations "
                            + conversation.getConversationId());
                    mongoConversation = new com.vedantu.comm.models.mongo.Conversation();
                }
                mongoConversation.conversationId = conversation.getConversationId();
                mongoConversation.orgId = conversation.orgId;
                mongoConversation.participants = new ArrayList<AddedMember>();
                for (SrcEntity hbaseConversationParticipant : conversation.getParticipants()) {
                    AddedMember addedMember = new AddedMember();
                    addedMember.member = hbaseConversationParticipant;
                    addedMember.timeAdded = conversation.getTimestamp();
                    mongoConversation.participants.add(addedMember);
                }
                mongoConversation.subject = conversation.getSubject();

                mongoConversation.recentMessageId = conversation.getRecentMessageId();
                Message hbaseMessage = oldMessageWrapper.getExact(
                        mongoConversation.recentMessageId, "messages", "data");
                mongoConversation.recentMessageTime = hbaseMessage.getSentOnTimestamp(); // fetch it
                // from
                // recentMessageId

                mongoConversation.firstMesssageId = conversation.getFirstMesssageId();
                Message hbaseFirstMessage = oldMessageWrapper.getExact(
                        mongoConversation.firstMesssageId, "messages", "data");

                mongoConversation.timeCreated = hbaseFirstMessage.getSentOnTimestamp(); // fetch it
                // from
                // recentMessageId
                mongoConversation.userId= hbaseFirstMessage.sender.id;
                mongoConversation.lastUpdated = hbaseMessage.getSentOnTimestamp();
                mongoConversation.messageCount = conversation.getMessageCount();
                mongoConversation.totalParticipants= mongoConversation.participants.size();
                ConversationDAO.INSTANCE.save(mongoConversation);
                // hbaseMessage.conversationId = mongoConversation._getStringId();
                // hbaseFirstMessage.conversationId = mongoConversation._getStringId();

                // newMessageWrapper.addData(hbaseFirstMessage.getKey(), "messages", "data",
                // hbaseFirstMessage);
                // newMessageWrapper.addData(hbaseMessage.getKey(), "messages", "data",
                // hbaseMessage);
                currentTimeStamp = conversation.getTimestamp() > currentTimeStamp ? conversation
                        .getTimestamp() : currentTimeStamp;

                excludeRowId = conversation.getConversationId();
                LOGGER.debug("Conversation id " + excludeRowId);
            }

        }
        migrateMessages();

    }

    public static void migrateMessages() throws IOException, VedantuException {

        HbaseTableWrapper<Message> oldMessageWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES),
                Message.class);

        HbaseTableWrapper<Message> newMessageWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES_V2),
                Message.class);
        long currentTimeStamp = System.currentTimeMillis() - 365 * 24 * 3600 * 1000;
        long millsecondsIn24Hrs = 24 * 3600 * 1000;
        final long timeStamp = System.currentTimeMillis();
        String excludeRowId = null;
        while (currentTimeStamp < timeStamp) {

            List<Message> hbaseMessages = oldMessageWrapper.getData(excludeRowId, "messages",
                    "data", null);
            if (CollectionUtils.isEmpty(hbaseMessages)) {
                LOGGER.debug("No hbase conversations found");
                currentTimeStamp = currentTimeStamp + millsecondsIn24Hrs;
                continue;
            }
            LOGGER.debug("Messages count " + hbaseMessages.size());
            com.vedantu.comm.models.mongo.Conversation mongoConversation = null;
            for (Message hb : hbaseMessages) {
                LOGGER.debug("message " + hb);
                excludeRowId = hb.getKey();
                LOGGER.debug("looking for conversation wht hbase id " + hb.getConversationId());
                mongoConversation = ConversationDAO.INSTANCE.find(hb.getConversationId());
                if (mongoConversation == null) {
                    LOGGER.debug("mongo conversation not found" + hb.getConversationId());
                    continue;
                }

                hb.conversationId = mongoConversation._getStringId();
                hb.receivers.remove(hb.sender);
                hb.setSentOnTimestamp(hb.getSentOnTimestamp());
                newMessageWrapper.addData(hb.getKey(), "messages", "data", hb);

                currentTimeStamp = hb.getTimestamp() > currentTimeStamp ? hb.getTimestamp()
                        : currentTimeStamp;

                LOGGER.debug("HBASE message id " + excludeRowId);
            }

        }

    }
}
