package com.vedantu.comm.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.common.Required;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.comm.daos.ConversationDAO;
import com.vedantu.comm.daos.ConversationSummaryDAO;
import com.vedantu.comm.daos.UserMailBoxInfoDAO;
import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.comm.enums.MessageAction;
import com.vedantu.comm.event.details.MessageDistributeDetails;
import com.vedantu.comm.models.hbase.messages.Message;
import com.vedantu.comm.models.hbase.messages.MessageSummary;
import com.vedantu.comm.models.mongo.Conversation;
import com.vedantu.comm.models.mongo.ConversationSummary;
import com.vedantu.comm.models.mongo.UserMailBoxInfo;
import com.vedantu.comm.pojos.AddedMember;
import com.vedantu.comm.requests.messages.DeleteConversationReq;
import com.vedantu.comm.requests.messages.GetConversationReq;
import com.vedantu.comm.requests.messages.GetConversationSummariesReq;
import com.vedantu.comm.requests.messages.GetConversationSummaryReq;
import com.vedantu.comm.requests.messages.GetConversationUsersReq;
import com.vedantu.comm.requests.messages.GetMessageReq;
import com.vedantu.comm.requests.messages.GetMessageSummariesReq;
import com.vedantu.comm.requests.messages.MarkConversationReq;
import com.vedantu.comm.requests.messages.SendMessageReq;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.comm.utils.IDGenerator;
import com.vedantu.comm.utils.MessageUtil;
import com.vedantu.comm.utils.UUIDGenerator;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.pojos.response.messages.DeleteConversationRes;
import com.vedantu.pojos.response.messages.GetConversationSummariesRes;
import com.vedantu.pojos.response.messages.GetConversationSummaryRes;
import com.vedantu.pojos.response.messages.GetConversationUsersRes;
import com.vedantu.pojos.response.messages.GetMessageRes;
import com.vedantu.pojos.response.messages.GetMessageSummariesRes;
import com.vedantu.pojos.response.messages.MarkConversationRes;
import com.vedantu.pojos.response.messages.SendMessageRes;
import com.vedantu.pojos.response.messages.UpdatedUserMailBoxInfoRes;
import com.vedantu.pojos.response.messages.mongo.ConversationSummaryBasicInfo;
import com.vedantu.pojos.response.messages.mongo.GetConversationRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.UserInfo;

public class MessageManager2 extends AbstractContentManager {

    private static final String               MAXIMUM_INVERSE_TIMESTAMP_LENGHT_020D  = "%020d";
    private final int                         USER_CONVERSAION_USER_ID_INDEX         = 0;
    private final int                         USER_CONVERSAION_CONVERSATION_ID_INDEX = 1;

    private static final ALogger              LOGGER                                 = Logger.of(MessageManager2.class);
    private IDGenerator                       idGenerator                            = null;
    private static MessageManager2 instance                               = null;
    private static final int                  MAXIMUM_BATCH_COUNT                    = 30;

    public static MessageManager2 get() {

        if (instance == null) {
            synchronized (MessageManager2.class) {
                if (instance == null) {
                    instance = new MessageManager2();
                }

            }
        }
        return instance;
    }

    private MessageManager2() {

        idGenerator = (IDGenerator) new UUIDGenerator();
    }

    private String getUserMessageId(SrcEntity user, Message message) {

        String userConversationId = getUserConversationId(user, message.getConversationId());
        if (StringUtils.isEmpty(userConversationId) || StringUtils.isEmpty(message.getMessageId())) {
            LOGGER.error(" failed conversationId" + userConversationId);
            LOGGER.error(" failed messageId" + userConversationId);

            return null;

        }
        StringBuilder builder = new StringBuilder();
        builder.append(userConversationId)
                .append("_")
                .append(String.format(MAXIMUM_INVERSE_TIMESTAMP_LENGHT_020D, Long.MAX_VALUE
                        - message.getSentOnTimestamp())).append("_").append(message.getMessageId());
        return builder.toString();

    }

    public SendMessageRes sendMessage(SendMessageReq request) throws VedantuException {

        Message message = request.message;
        if (message.sender == null || !message.sender.id.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }

        int retries = 0;
        SendMessageRes response = null;

        while (retries < 5) {
            try {

                if (StringUtils.isEmpty(message.orgId) && StringUtils.isNotEmpty(request.orgId)) {
                    message.orgId = request.orgId;

                }
                MessageAction action = message.getAction();

                HbaseTableWrapper<Message> messageTableWrapper = new HbaseTableWrapper<Message>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_MESSAGES_V2), Message.class);

                String messageId = idGenerator.getID();
                message.setMessageId(messageId);
                message.removeImageSrc(true);
                message.setSentOnTimestamp(System.currentTimeMillis());
                message.setAction(action);

                Conversation conversation = null;
                if (StringUtils.isNotEmpty(message.getConversationId())) {

                    conversation = ConversationDAO.INSTANCE.getById(message.getConversationId());

                }
                if (conversation == null) {

                    conversation = new Conversation();
                    // conversation.setConversationId(conversationId);
                    conversation.subject = message.subject;
                    List<AddedMember> deduplicator = new ArrayList<AddedMember>();

                    for (SrcEntity srcUser : message.getReceivers()) {
                        AddedMember addMember = new AddedMember();
                        addMember.member = srcUser;
                        addMember.timeAdded = message.getSentOnTimestamp();

                        deduplicator.add(addMember);
                    }

                    // adding sender to participants
                    AddedMember addMember = new AddedMember();
                    addMember.member = message.getSender();
                    addMember.timeAdded = message.getSentOnTimestamp();

                    deduplicator.add(addMember);

                    conversation.participants = deduplicator;
                    conversation.recentMessageId = messageId;
                    conversation.firstMesssageId = messageId;
                    conversation.totalParticipants = deduplicator.size();
                    conversation.userId = message.sender.id;
                    ConversationDAO.INSTANCE.save(conversation);

                } else {
                    // TODO add if conversation already exists

                    if (CollectionUtils.isEmpty(message.getReceivers())
                            && CollectionUtils.isNotEmpty(conversation.participants)) {
                        List<SrcEntity> participants = new ArrayList<SrcEntity>();
                        for (AddedMember user : conversation.participants) {
                            if (!user.member.id.equals(message.sender.id)) {
                                participants.add(user.member);
                            }
                        }

                        message.setReceivers(participants);
                    }

                    message.setSubject(conversation.subject);

                }

                message.setConversationId(conversation._getStringId());
                messageTableWrapper.addData(message.getKey(), "messages", "data", message);

                ConversationDAO.INSTANCE.updateIncForNewMessage(conversation._getStringId());

                ConversationDAO.INSTANCE.updateRecentMessageInfo(conversation._getStringId(),
                        message.getSentOnTimestamp(), message.getKey());

                /**
                 * convert it to async messages and if saving fails send back failure message
                 */
                MessageSummary messageSummary = new MessageSummary(message.getMessageId());
                messageSummary.setContent(message.getContent());
                messageSummary.setReceiver(message.getSender());
                messageSummary.setSender(message.getSender());
                messageSummary.setConversationId(conversation._getStringId()); // very
                                                                               // compulsory
                messageSummary.setReceivedTime(System.currentTimeMillis());
                messageSummary.setSentTime(message.getSentOnTimestamp());
                messageSummary.setStatus(ConversationStatus.READ);

                ConversationSummary olderConversationSummary = ConversationSummaryDAO.INSTANCE
                        .update(messageSummary.conversationId, message.getSender().id, true,
                                conversation.firstMesssageId, conversation.participants.size(), conversation.orgId,
                                messageSummary.getSentTime());

                boolean isNewConversation = false;
                if (olderConversationSummary == null
                        || olderConversationSummary.recordState == VedantuRecordState.DELETED) {
                    isNewConversation = true;

                }

                HbaseTableWrapper<MessageSummary> userMessageTableWrapper = new HbaseTableWrapper<MessageSummary>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2),
                        MessageSummary.class);
                LOGGER.debug(" Saving user messages " + messageSummary);
                userMessageTableWrapper.addData(messageSummary.getNewKey(), "messages", "data",
                        messageSummary);

                ConversationSummaryDAO.INSTANCE.updateMessageSummary(messageSummary.conversationId,
                        messageSummary.userId, messageSummary.content, message.getSubject(),
                        messageSummary.getSentTime(), messageSummary.sender.id,
                        messageSummary.messageId);

                UserMailBoxInfoDAO.INSTANCE.updateCounts(message.getSender().id,
                        isNewConversation ? true : false, false, isNewConversation);

                MessageDistributeDetails distributeDetails = new MessageDistributeDetails();
                distributeDetails.messageId = message.getKey();
                distributeDetails.conversationId = conversation._getStringId();
                generateEventAysc(messageSummary.sender.id, distributeDetails,
                        EventType.MESSAGE_DISTRIBUTE);

                messageSummary.setSender(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        messageSummary.getSender()));
                annotateImages(messageSummary);
                response = new SendMessageRes(true, messageSummary);
                return response;
            } catch (VedantuException exception) {

                LOGGER.error("Exception occured while sending message" + exception.getClass() + " "
                        + exception.getMessage(), exception);
                retries++;
            } catch (Exception exception) {
                LOGGER.error("Exception occured while sending message" + exception.getClass() + " "
                        + exception.getMessage(), exception);
                retries++;
            }

        }
        throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED);

    }

    private void annotateImages(IReverseImageMapperProcessor content) {

        content.addImageSrcUrl();
    }

    public GetConversationSummariesRes
            getConversationSummaries(GetConversationSummariesReq request) throws VedantuException {

        MutableLong totalHits = new MutableLong(0);
        List<ConversationSummary> summaries = ConversationSummaryDAO.INSTANCE.get(request.userId,
                request.start, request.size, totalHits, VedantuRecordState.ACTIVE);

        List<com.vedantu.comm.models.hbase.messages.ConversationSummary> hbaseSummaries = getHbaseConversationSummary(
                request.orgId, summaries);

        GetConversationSummariesRes response = new GetConversationSummariesRes();
        response.list.addAll(hbaseSummaries);
        response.totalHits = totalHits.longValue();

        return response;

    }

    private List<ConversationSummaryBasicInfo> annotateExtraInfo(String orgId,
            List<ConversationSummary> summaries) {

        if (CollectionUtils.isEmpty(summaries)) {
            return null;
        }
        Set<String> userIds = new HashSet<String>();

        for (ConversationSummary summary : summaries) {
            userIds.add(summary.mostRecentSenderId);
            userIds.add(summary.userId);
        }
        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds, true);

        List<ConversationSummaryBasicInfo> conversationSummaryBasicInfos = new ArrayList<ConversationSummaryBasicInfo>();
        for (ConversationSummary summary : summaries) {

            ConversationSummaryBasicInfo info = (ConversationSummaryBasicInfo) summary
                    .toBasicInfo();

            info.mostRecentSender = (UserInfo) userInfoMap.get(info.mostRecentSender.id);
            info.userInfo = (UserInfo) userInfoMap.get(info.userInfo.id);
            info.addImageSrcUrl();
            conversationSummaryBasicInfos.add(info);

        }
        return conversationSummaryBasicInfos;

    }

    public GetConversationRes getConversation(GetConversationReq request) throws VedantuException {

        Conversation conversation = getConversation(request.userId, request.conversationId);

        GetConversationRes reservationRes = new GetConversationRes();
        reservationRes.conversation = conversation;
        return reservationRes;
    }

    public Conversation getConversation(@Required String userId, @Required String conversationId)
            throws VedantuException {

        Conversation conversation = ConversationDAO.INSTANCE.getById(conversationId,
                VedantuRecordState.ACTIVE);

        return conversation;
    }

    public GetConversationUsersRes getConversationUsers(GetConversationUsersReq request)
            throws VedantuException {

        GetConversationUsersRes response = new GetConversationUsersRes();

        MutableLong totalHits = new MutableLong();
        Conversation conversation = ConversationDAO.INSTANCE.getConversation(
                request.conversationId, request.start, request.size, totalHits);

        if (conversation == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_CONVERSATION_ID);

        }

        if (CollectionUtils.isNotEmpty(conversation.participants)) {
            LOGGER.debug(" NUmber of participants in this list " + conversation.participants);
            List<String> list = new ArrayList<String>();
            for (AddedMember member : conversation.participants) {
                list.add(member.member.id);
            }

            if (CollectionUtils.isNotEmpty(request.excludeUserIds)) {
                list.removeAll(request.excludeUserIds);
            }
            Collections.sort(list);
            LOGGER.debug(" Populating users now ");
            Map<String, ModelBasicInfo> userInfos = getUserInfoMap(request.orgId, list);

            response.list.addAll(userInfos.values());

            response.totalHits = conversation.totalParticipants;

            return response;
        }
        LOGGER.debug(" No  of participants in this list ");
        return null;

    }

    /** temporary solution */
    public GetConversationSummaryRes getConversationSummary(GetConversationSummaryReq request) {

        String[] splittedUserConversation = request.userConversationId.split("_");

        ConversationSummary mongoSummary = ConversationSummaryDAO.INSTANCE.find(
                splittedUserConversation[1], splittedUserConversation[0]);

        List<ConversationSummary> mongoSummaries = new ArrayList<ConversationSummary>();
        mongoSummaries.add(mongoSummary);

        List<com.vedantu.comm.models.hbase.messages.ConversationSummary> hbaseConversationSummaries = getHbaseConversationSummary(
                request.orgId, mongoSummaries);
        com.vedantu.comm.models.hbase.messages.ConversationSummary hbaseConSummary = hbaseConversationSummaries
                .get(0);

        GetConversationSummaryRes response = new GetConversationSummaryRes();
        response.summary = hbaseConSummary;
        return response;

    }

    private String getUserConversationId(SrcEntity user, String conversationId) {

        StringBuilder builder = new StringBuilder();
        builder.append(user.id).append("_").append(conversationId);
        return builder.toString();
    }

    public List<com.vedantu.comm.models.hbase.messages.ConversationSummary>
            getHbaseConversationSummary(String orgId, List<ConversationSummary> mongoSummaries) {

        List<com.vedantu.comm.models.hbase.messages.ConversationSummary> hbaseConversationSummaries = new ArrayList<com.vedantu.comm.models.hbase.messages.ConversationSummary>();
        for (ConversationSummary mongoSummary : mongoSummaries) {
            com.vedantu.comm.models.hbase.messages.ConversationSummary hbaseConversationSummary = new com.vedantu.comm.models.hbase.messages.ConversationSummary();

            hbaseConversationSummary.content = mongoSummary.content;
            hbaseConversationSummary.subject = mongoSummary.subject;
            hbaseConversationSummary.firstMessageId = mongoSummary.firstMessageId;
            hbaseConversationSummary.mostRecentMessageTiming = mongoSummary.mostRecentMessageTime;
            hbaseConversationSummary.mostRecentSender = new SrcEntity(EntityType.USER,
                    mongoSummary.mostRecentSenderId);
            hbaseConversationSummary.messageCount = mongoSummary.messageCount;
            hbaseConversationSummary.messagesUnread = mongoSummary.messagesUnread;
            hbaseConversationSummary.numOfParticipants = mongoSummary.numOfParticipants;
            hbaseConversationSummary.orgId = mongoSummary.orgId;
            hbaseConversationSummary.userConversationId = getUserConversationId(new SrcEntity(
                    EntityType.USER, mongoSummary.userId), mongoSummary.conversationId);
            hbaseConversationSummary.conversationId = mongoSummary.conversationId;

            hbaseConversationSummary.setStatus(mongoSummary.status);

            hbaseConversationSummary.setTimestamp(hbaseConversationSummary.mostRecentMessageTiming);

            hbaseConversationSummary.setMostRecentSender(MessageUtil.populateUserNewsEntityDetails(
                    orgId, hbaseConversationSummary.getMostRecentSender()));
            annotateImages(hbaseConversationSummary);

            hbaseConversationSummaries.add(hbaseConversationSummary);

        }
        return hbaseConversationSummaries;
    }

    public MarkConversationRes markConversation(MarkConversationReq request)
            throws VedantuException {

        boolean success = markConversation(request.userId, request.userConversationId,
                request.status);

        return new MarkConversationRes(success, request.userConversationId);

    }

    private boolean markConversation(@Required String userId, @Required String userConversationId,
            @Required ConversationStatus newStatus) throws VedantuException {

        String[] userConversationSplitted = userConversationId.split("_");
        LOGGER.debug("Marking conversation " + userConversationId + " status " + newStatus);

        ConversationSummary conversationSummary = ConversationSummaryDAO.INSTANCE.find(
                userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX],
                userConversationSplitted[USER_CONVERSAION_USER_ID_INDEX]);

        LOGGER.debug("Current  conversation " + userConversationId + " status "
                + conversationSummary.status);
        if (conversationSummary.status == newStatus) {
            LOGGER.debug("No status update as  both requested and existing status is same"
                    + userConversationId + " status " + conversationSummary.status);
            return true;
        }

        // TODO took following code for marking message to new status in background check with
        // current time
        //        int messageCount = 0;
        // HbaseTableWrapper<MessageSummary> userMessageWrapper = new
        // HbaseTableWrapper<MessageSummary>(
        // Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
        // MessageSummary.class);

        // if (conversationSummary != null) {
        // int MAXIMUM_FETCH_COUNT = 30;
        // List<MessageSummary> messageSummaries = null;
        // Map<String, AbstractHbaseModels> messageKeyMap = new HashMap<String,
        // AbstractHbaseModels>();
        // do {
        //
        // messageSummaries = MessageManager.get().getMessageSummaries(userId,
        // conversationSummary.conversationId, MAXIMUM_FETCH_COUNT);
        // LOGGER.debug(" FOund message count " + messageSummaries.size());
        // for (MessageSummary summary : messageSummaries) {
        // summary.setStatus(newStatus);
        // // userMessageWrapper.addData(summary.getKey(), "messages", "data", summary);
        // LOGGER.debug(" New status will be " + newStatus);
        // messageKeyMap.put(summary.getKey(), summary);
        // messageCount++;
        // }
        //
        // } while (CollectionUtils.isNotEmpty(messageSummaries)
        // && messageSummaries.size() >= MAXIMUM_FETCH_COUNT);
        //
        // userMessageWrapper.addData(messageKeyMap, "messages", "data");

        // if (newStatus == ConversationStatus.UNREAD) {
        // ConversationSummaryDAO.INSTANCE.incMessageUnread(
        // conversationSummary._getStringId(), conversationSummary.messageCount);
        // } else {
        // ConversationSummaryDAO.INSTANCE.decMessageUnread(
        // conversationSummary._getStringId(), messageCount);
        // }
        //
        // conversationSummary.status = newStatus;
        // ConversationSummaryDAO.INSTANCE.updateModel(conversationSummary,
        // Arrays.asList("status"));

        //

        conversationSummary = ConversationSummaryDAO.INSTANCE.resetMessageUnread(
                conversationSummary._getStringId(), newStatus);

        if (newStatus == ConversationStatus.UNREAD) {
            UserMailBoxInfoDAO.INSTANCE.incUnreadConversations(userId, 1);
        } else {
            UserMailBoxInfoDAO.INSTANCE.decUnreadConversations(userId, 1);

        }

        // }
        // TODO see if all messages in conversation mark read
        return true;
    }

    public DeleteConversationRes deleteUserConversation(DeleteConversationReq request)
            throws VedantuException {

        boolean isDeleted = deleteUserConversation(request.userId, request.userConversationId);
        DeleteConversationRes response = new DeleteConversationRes();
        response.deleted = isDeleted;
        return response;
    }

    public boolean deleteUserConversation(@Required String userId,
            @Required String userConversationId) throws VedantuException {

        if (userConversationId.startsWith(userId)) {
            String[] userConversationSplitted = userConversationId.split("_");
            // delete all other messages
            Conversation conversation = ConversationDAO.INSTANCE
                    .getById(userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX]);

            ConversationSummary olderSummary = ConversationSummaryDAO.INSTANCE.markDeleted(
                    userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX],
                    userConversationSplitted[USER_CONVERSAION_USER_ID_INDEX]);
            UserMailBoxInfoDAO.INSTANCE.deleteConversation(userId,
                    olderSummary.status == ConversationStatus.UNREAD,
                    conversation.userId.equals(olderSummary.userId));

            return true;
        }
        return false;
    }

    // public boolean deleteUserMessage(@Required String userId, @Required String userMessageId)
    // throws VedantuException {
    //
    // if (userMessageId.startsWith(userId)) {
    // HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
    // Play.application().configuration()
    // .getString(MessageUtil.TABLE_NAME_USER_MESSAGE), MessageSummary.class);
    //
    // HbaseTableWrapper<ConversationSummary> userConversationWrapper = new
    // HbaseTableWrapper<ConversationSummary>(
    // Play.application().configuration()
    // .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
    // ConversationSummary.class);
    //
    // MessageSummary messageSummary = userMessageWrapper.getExact(userMessageId, "messages",
    // "data");
    //
    // boolean deleteStatus = userMessageWrapper.delete(userMessageId);
    //
    // if (deleteStatus) {
    // ConversationSummary conversationSummary = userConversationWrapper.getExact(
    // getUserConversationId(new SrcEntity(EntityType.USER, userId),
    // messageSummary.getConversationId()), "conversations", "data");
    //
    // StringBuilder regexBuilder = new StringBuilder();
    // regexBuilder.append(userId).append("_").append(".*").append("_")
    // .append(conversationSummary.getConversationId()).append("_");
    //
    // MessageSummary nextMessageSummary = userMessageWrapper.getNextData(userMessageId,
    // "messages", "data", regexBuilder.toString());
    //
    // if (nextMessageSummary == null) {
    // deleteUserConversation(userId, conversationSummary.getConversationId());
    // } else {
    // conversationSummary.setContent(nextMessageSummary.getContent());
    // userConversationWrapper.addData(conversationSummary.getUserConversationId(),
    // "conversations", "data", conversationSummary);
    // }
    //
    // }
    // }
    // return false;
    // }

    public UpdatedUserMailBoxInfoRes updateUsersMailBoxesInfos(List<String> userIds)
            throws VedantuException {

        long currentTime = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<User> users = UserDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(userIds));

            for (User user : users) {
                updateUserMailBoxInfo(user._getStringId());
            }
        } else {

            int currentBatchStart = 0;
            userIds = new ArrayList<String>();
            do {
                Set<String> userSet = UserDAO.INSTANCE.getIdsByTime(0, currentTime,
                        currentBatchStart, MAXIMUM_BATCH_COUNT);
                userIds.clear();
                userIds.addAll(userSet);
                if (CollectionUtils.isNotEmpty(userIds)) {
                    for (String userid : userIds) {
                        updateUserMailBoxInfo(userid);
                    }
                }
                currentBatchStart = currentBatchStart + MAXIMUM_BATCH_COUNT;
            } while (CollectionUtils.isNotEmpty(userIds) && userIds.size() == MAXIMUM_BATCH_COUNT);

        }
        UpdatedUserMailBoxInfoRes response = new UpdatedUserMailBoxInfoRes();
        response.updated = true;
        return response;
    }

    private boolean updateUserMailBoxInfo(String userId) throws VedantuException {

        Logger.debug(" Updating mail box for " + userId);
        List<ConversationSummary> conversationList;
        UserMailBoxInfo info = UserMailBoxInfoDAO.INSTANCE.getByUserId(userId);
        StringBuilder builder = new StringBuilder();
        builder.append(userId).append("_");

        info.unreadConversationCount = 0;
        info.conversationCount = 0;
        info.sentCount = 0;
        UserMailBoxInfoDAO.INSTANCE.save(info);
        int start = 0;
        do {
            MutableLong totalConversations = new MutableLong();
            conversationList = ConversationSummaryDAO.INSTANCE.get(userId, start,
                    MAXIMUM_BATCH_COUNT, totalConversations, VedantuRecordState.ACTIVE);

            if (CollectionUtils.isNotEmpty(conversationList)) {
                for (ConversationSummary conversationSummary : conversationList) {
                    LOGGER.debug(" userId " + userId + " Current conversation summary "
                            + conversationSummary.status);
                    Conversation conversation = ConversationDAO.INSTANCE
                            .getById(conversationSummary.conversationId);
                    UserMailBoxInfoDAO.INSTANCE.updateCounts(userId,
                            conversation.userId.equals(userId),
                            conversationSummary.status == ConversationStatus.UNREAD, true);
                    start = start + MAXIMUM_BATCH_COUNT;
                }
            }

        } while (CollectionUtils.isNotEmpty(conversationList)
                && conversationList.size() == MAXIMUM_BATCH_COUNT);

        return true;
    }

    public GetMessageSummariesRes getMessageSummaries(GetMessageSummariesReq request)
            throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);

        // TODO make it async
        ConversationSummary conversation = ConversationSummaryDAO.INSTANCE.find(
                request.conversationId, request.userId);
        totalHits.setValue(conversation.messageCount);

        int resultSize = (conversation.messageCount - 1) > 0 ? conversation.messageCount - 1 : 0;
        request.size = (resultSize < request.size) ? resultSize : request.size;

        LOGGER.debug("Fetching messages " + request.size + " request.userMesageId"
                + request.userMessageId);
        List<MessageSummary> messageSummaryList = null;
        if (StringUtils.isNotEmpty(request.userMessageId)) {
            messageSummaryList = getMessageSummaries(request.userId, request.userMessageId,
                    request.conversationId, request.size);
        } else {
            messageSummaryList = getMessageSummaries(request.userId, request.conversationId,
                    request.size);
        }
        if (CollectionUtils.isNotEmpty(messageSummaryList)) {

            for (MessageSummary summary : messageSummaryList) {
                summary.userMessageId = summary.getNewKey();
                summary.setSender(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        summary.getSender()));
                summary.setReceiver(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        summary.getReceiver()));
                annotateImages(summary);
                LOGGER.trace("Content" + summary.getContent());
            }
        }

        GetMessageSummariesRes response = new GetMessageSummariesRes();
        response.totalHits = totalHits.longValue();
        response.list.addAll(messageSummaryList);
        return response;
    }

    List<MessageSummary> getMessageSummaries(@Required String userId,
            @Required String conversationId, int count) throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2), MessageSummary.class);
        StringBuilder builder = new StringBuilder();
        builder.append(userId)
                .append("_")
                .append(conversationId)
                .append("_")
                .append(String.format(MAXIMUM_INVERSE_TIMESTAMP_LENGHT_020D, Long.MAX_VALUE
                        - System.currentTimeMillis())).append("_");
        StringBuilder prefix = new StringBuilder();
        prefix.append(userId).append("_").append(conversationId).append("_");
        List<MessageSummary> messageSummaryList = userMessageWrapper.getData(builder.toString(),
                "messages", "data", count, prefix.toString(), false);
        return messageSummaryList;
    }

    public List<MessageSummary> getMessageSummaries(@Required String userId,
            @Required String userMessageId, @Required String conversationId, int count)
            throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageTableWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2), MessageSummary.class);

        StringBuilder prefix = new StringBuilder();
        prefix.append(userId).append("_").append(conversationId).append("_");

        List<MessageSummary> messageSummaryList = userMessageTableWrapper.getData(userMessageId,
                "messages", "data", count, prefix.toString(), false);

        return messageSummaryList;

    }

    public MessageSummary getMessageSummary(@Required String orgId, @Required String userId,
            @Required String userMessageId) throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2), MessageSummary.class);
        MessageSummary summary = userMessageWrapper.getExact(userMessageId, "messages", "data");

        summary.setSender(MessageUtil.populateUserNewsEntityDetails(orgId, summary.getSender()));
        annotateImages(summary);
        return summary;
    }

    public GetMessageRes getMessage(GetMessageReq request) throws VedantuException {

        Message message = getMessage(request.userId, request.messageId);

        LOGGER.debug("Decorating Message:" + message);
        message.setSender(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                message.getSender()));
        SrcEntity requester = new SrcEntity(EntityType.USER, request.userId);
        List<SrcEntity> receivers = message.getReceivers();
        receivers.remove(requester);

        List<SrcEntity> decoratedReceivers = MessageUtil.populateUserNewsEntityDetails(
                request.orgId, receivers);
        message.setReceivers(decoratedReceivers);
        GetMessageRes response = new GetMessageRes();
        response.message = message;
        annotateImages(message);
        return response;
    }

    private Message getMessage(@Required String userId, @Required String messageId)
            throws VedantuException {

        HbaseTableWrapper<Message> messageTableWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES_V2),
                Message.class);

        Message message = messageTableWrapper.getExact(messageId, "messages", "data");

        return message;

    }

}
