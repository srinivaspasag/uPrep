package com.vedantu.comm.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Required;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.comm.daos.UserMailBoxInfoDAO;
import com.vedantu.comm.email.details.MessageEmailDetails;
import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.comm.enums.MessageAction;
import com.vedantu.comm.models.hbase.messages.Conversation;
import com.vedantu.comm.models.hbase.messages.ConversationSummary;
import com.vedantu.comm.models.hbase.messages.Message;
import com.vedantu.comm.models.hbase.messages.MessageSummary;
import com.vedantu.comm.models.mongo.UserMailBoxInfo;
import com.vedantu.comm.requests.messages.DeleteConversationReq;
import com.vedantu.comm.requests.messages.GetConversationReq;
import com.vedantu.comm.requests.messages.GetConversationSummariesReq;
import com.vedantu.comm.requests.messages.GetConversationSummaryReq;
import com.vedantu.comm.requests.messages.GetConversationUsersReq;
import com.vedantu.comm.requests.messages.GetMessageReq;
import com.vedantu.comm.requests.messages.GetMessageSummariesReq;
import com.vedantu.comm.requests.messages.GetUserMailBoxInfoReq;
import com.vedantu.comm.requests.messages.MarkConversationReq;
import com.vedantu.comm.requests.messages.SendMessageReq;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.comm.utils.IDGenerator;
import com.vedantu.comm.utils.MessageUtil;
import com.vedantu.comm.utils.UUIDGenerator;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.Organization;
import com.vedantu.pojos.response.messages.DeleteConversationRes;
import com.vedantu.pojos.response.messages.GetConversationRes;
import com.vedantu.pojos.response.messages.GetConversationSummariesRes;
import com.vedantu.pojos.response.messages.GetConversationSummaryRes;
import com.vedantu.pojos.response.messages.GetConversationUsersRes;
import com.vedantu.pojos.response.messages.GetMessageRes;
import com.vedantu.pojos.response.messages.GetMessageSummariesRes;
import com.vedantu.pojos.response.messages.GetUserMailBoxInfoRes;
import com.vedantu.pojos.response.messages.MarkConversationRes;
import com.vedantu.pojos.response.messages.SendMessageRes;
import com.vedantu.pojos.response.messages.UpdatedUserMailBoxInfoRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.UserEmailInfo;
import com.vedantu.user.pojos.UserExtendedInfo;
@Deprecated
public class MessageManager extends AbstractContentManager {

    private static final ALogger  LOGGER              = Logger.of(MessageManager.class);
    private IDGenerator           idGenerator         = null;
    private static MessageManager instance            = null;
    private static final int      MAXIMUM_BATCH_COUNT = 30;

    public static MessageManager get() {

        if (instance == null) {
            synchronized (MessageManager.class) {
                if (instance == null) {
                    instance = new MessageManager();
                }

            }
        }
        return instance;
    }

    private MessageManager() {

        idGenerator = (IDGenerator) new UUIDGenerator();
    }

    public static String getUserConversationId(SrcEntity user, String conversationId) {

        StringBuilder builder = new StringBuilder();
        builder.append(user.id).append("_").append(conversationId);
        return builder.toString();
    }

    public String getUserMessageId(SrcEntity user, Message message) {

        String userConversationId = getUserConversationId(user, message.getConversationId());
        if (StringUtils.isEmpty(userConversationId) || StringUtils.isEmpty(message.getMessageId())) {
            return null;

        }
        StringBuilder builder = new StringBuilder();
        builder.append(userConversationId).append("_")
                .append(String.format("%020d", Long.MAX_VALUE - System.currentTimeMillis()))
                .append("_").append(message.getMessageId());
        return builder.toString();

    }

    public SendMessageRes sendMessage(SendMessageReq request) throws VedantuException {

        // verfication for email messages
        Message message = request.message;
        if (message.sender == null || !message.sender.id.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }

        OrgMember senderMember = OrgMemberDAO.INSTANCE.getMemberByUserId(request.orgId,
                request.userId);

        int retries = 0;
        SendMessageRes response = null;
        MessageEmailDetails messageEmailDetails = new MessageEmailDetails();
        messageEmailDetails.senderInfo.firstName = senderMember.firstName;
        messageEmailDetails.orgId = senderMember.orgId;
        messageEmailDetails.senderInfo.id = senderMember.userId;

        while (retries < 5) {
            try {
                List<String> unverfiedEmailReceivers = new ArrayList<String>();

                if (StringUtils.isEmpty(message.orgId) && StringUtils.isNotEmpty(request.orgId)) {
                    message.orgId = request.orgId;

                    Organization org = OrganizationDAO.INSTANCE.getById(request.orgId);
                    messageEmailDetails.organizationName = org.name;

                }
                MessageAction action = message.getAction();

                HbaseTableWrapper<Message> messageTableWrapper = new HbaseTableWrapper<Message>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_MESSAGES), Message.class);
                HbaseTableWrapper<Conversation> conversationTableWrapper = new HbaseTableWrapper<Conversation>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_CONVERSATION), Conversation.class);

                HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                        ConversationSummary.class);
                HbaseTableWrapper<MessageSummary> userMessageTableWrapper = new HbaseTableWrapper<MessageSummary>(
                        Play.application().configuration()
                                .getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                        MessageSummary.class);
                LOGGER.info(" Message Receivers" + message.getReceivers());
                String messageId = idGenerator.getID();
                LOGGER.info(" MessageId generated " + messageId);
                message.setMessageId(messageId);

                message.removeImageSrc(true);

                message.setSentOnTimestamp(System.currentTimeMillis());

                String conversationId = message.getConversationId();
                Conversation conversation = null;
                conversation = conversationTableWrapper.getExact(conversationId, "conversations",
                        "data");

                if (conversation == null) {
                    conversationId = idGenerator.getID();
                    message.setConversationId(conversationId);

                    conversation = new Conversation();
                    conversation.setConversationId(conversationId);
                    conversation.setSubject(message.getSubject());
                    Set<SrcEntity> deduplicator = new HashSet<SrcEntity>();
                    deduplicator.addAll(message.getReceivers());
                    deduplicator.add(message.getSender());
                    conversation.setParticipants(new ArrayList(deduplicator));

                    conversation.setRecentMessageId(messageId);
                    conversation.setTimestamp(System.currentTimeMillis());
                    conversation.setFirstMesssageId(messageId);

                } else {
                    // TODO add if conversation already exists

                    conversation.setRecentMessageId(messageId);
                    if (CollectionUtils.isEmpty(message.getReceivers())) {
                        message.setReceivers(conversation.getParticipants());
                    }

                    message.setSubject(conversation.getSubject());

                }
                conversation.incrementMessageCount();
                message.setAction(action);

                MessageSummary messageSummary = new MessageSummary(message.getMessageId());
                String summaryText = null;
                if (message.getContent() != null
                        && message.getContent().length() > MessageSummary.CHARACTER_LIMIT) {
                    summaryText = message.getContent()
                            .substring(
                                    0,
                                    Math.min(MessageSummary.CHARACTER_LIMIT, message.getContent()
                                            .length()) - 1);
                }
                messageSummary.setStatus(ConversationStatus.UNREAD);
                messageSummary.setContent(message.getContent());
                // Update tables
                messageTableWrapper.addData(message.getKey(), "messages", "data", message);
                conversationTableWrapper.addData(conversation.getKey(), "conversations", "data",
                        conversation);

                Set<SrcEntity> forwardList = new HashSet<SrcEntity>();
                forwardList.addAll(message.getReceivers());
                forwardList.add(message.getSender());
                /**
                 * convert it to async messages and if saving fails send back failure message
                 */

                MessageSummary senderMesageSummary = null;
                LOGGER.info(" List" + forwardList + " receievd list" + message.getReceivers() + " "
                        + message.getSender());

                messageEmailDetails
                        .addHeader(MessageEmailDetails.X_CONVERSATION_ID, conversationId);
                messageEmailDetails.addHeader(MessageEmailDetails.X_MESSAGE_ID, messageId);

                for (SrcEntity receiver : forwardList) {

                    String userMessageId = getUserMessageId(receiver, message);

                    messageSummary.setUserMessageId(userMessageId);
                    messageSummary.setReceiver(receiver);

                    messageSummary.setSender(message.getSender());
                    messageSummary.setConversationId(conversation.getConversationId());

                    messageSummary.setReceivedTime(System.currentTimeMillis());
                    messageSummary.setSentTime(message.getSentOnTimestamp());

                    String receiverConversationSummaryId = getUserConversationId(receiver,
                            conversationId);

                    ConversationSummary conversationSummary = userConversationTableWrapper
                            .getExact(receiverConversationSummaryId, "conversations", "data");
                    // move to converation manager
                    DBObject userMailBoxQuery = new BasicDBObject();
                    userMailBoxQuery.put(ConstantsGlobal.USER_ID, receiver.id);
                    UserMailBoxInfo userMailBoxInfo = UserMailBoxInfoDAO.INSTANCE
                            .getByUserId(receiver.id);

                    if (conversationSummary == null) {
                        conversationSummary = new ConversationSummary();
                        conversationSummary.setFirstMessageId(conversation.getFirstMesssageId());
                        userMailBoxInfo.incrementConversationCount();

                    } else {
                        LOGGER.info(" User Conversation is old and now updated");
                    }

                    LOGGER.info(" Incrementing unread conversations for receiver " + receiver.id
                            + conversationSummary.getStatus());
                    if (conversationSummary.getStatus() != ConversationStatus.UNREAD
                            && !receiver.equals(message.getSender())) {
                        LOGGER.info(" Incrementing unread conversations for "
                                + userMailBoxInfo.userId);
                        userMailBoxInfo.incrementUnread();
                        LOGGER.info(" Incrementing unread conversations with unread "
                                + userMailBoxInfo.unreadConversationCount);
                    }
                    // userMailBoxInfo.s

                    conversationSummary.setNumOfParticipants(conversation.getParticipants().size());
                    conversationSummary.setUserConversationId(receiverConversationSummaryId);
                    conversationSummary.setMostRecentSender(message.getSender());

                    conversationSummary.setContent(summaryText);
                    conversationSummary.incrementMessageCount();

                    conversationSummary.setConversationId(conversation.getConversationId());
                    conversationSummary.setSubject(conversation.getSubject());
                    conversationSummary.setMostRecentMessageTiming(message.getSentOnTimestamp());

                    if (receiver.equals(message.getSender())) {
                        messageSummary.setStatus(ConversationStatus.READ);
                        conversationSummary.setStatus(ConversationStatus.READ);
                        senderMesageSummary = messageSummary;
                        userMailBoxInfo.incrementSentCount();
                    }
                    userMessageTableWrapper.addData(messageSummary.getKey(), "messages", "data",
                            messageSummary);
                    userConversationTableWrapper.addData(conversationSummary.getKey(),
                            "conversations", "data", conversationSummary);

                    // email sending part
                    annotateImages(messageSummary);
                    messageEmailDetails.messageContent = messageSummary.content;
                    User user = UserDAO.INSTANCE.getById(receiver.id);
                    // TODO this part will send out emails for receivers it will need more
                    // decorations though
                    if (!receiver.equals(message.getSender())) {
                        if (user.isEmailVerified) {

                            messageEmailDetails.addRecepient(user._getFullName(), user.email);
                            messageEmailDetails.setSubject(conversationSummary.subject);
                            messageEmailDetails.addHeader(
                                    MessageEmailDetails.X_USER_CONVERSATION_ID,
                                    conversationSummary.userConversationId);
                            messageEmailDetails.addHeader(MessageEmailDetails.X_USER_MESSAGE_ID,
                                    messageSummary.messageId);
                            unverfiedEmailReceivers.remove(user.email);
                            UserEmailInfo userEmailInfo = new UserEmailInfo();
                            userEmailInfo.fromUserExtendedInfo((UserExtendedInfo) user.toExtendedInfo());
                            userEmailInfo.setCategory(MailCategory.MESSAGE);
                            
                            generateEventAysc(request.userId, messageEmailDetails,
                                    EventType.SEND_EMAIL);
                        }
                    }

                    UserMailBoxInfoDAO.INSTANCE.save(userMailBoxInfo);
                    // send out and notification for users here

                }

                senderMesageSummary.setSender(MessageUtil.populateUserNewsEntityDetails(
                        request.orgId, senderMesageSummary.getSender()));
                annotateImages(senderMesageSummary);
                response = new SendMessageRes(true, senderMesageSummary);
                return response;
            } catch (VedantuException exception) {
                // TODO Auto-generated catch block

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

    public GetConversationSummariesRes
            getConversationSummaries(GetConversationSummariesReq request) throws VedantuException {

        UserMailBoxInfo userMailBoxInfo = UserMailBoxInfoDAO.INSTANCE.getByUserId(request.userId);

        List<ConversationSummary> conversationList = null;
        boolean getFirstData = !(request.future);// get in future means get last values
        if (request.timestamp != -1L) {
            conversationList = getMoreConversationSummaries(request.userId, request.timestamp,
                    request.size, request.future);

        } else {
            conversationList = getConversationSummaries(request.userId, request.size);
        }
        if (CollectionUtils.isNotEmpty(conversationList)) {
            for (ConversationSummary summary : conversationList) {
                summary.setMostRecentSender(MessageUtil.populateUserNewsEntityDetails(
                        request.orgId, summary.getMostRecentSender()));
                annotateImages(summary);
            }

            Collections.sort(conversationList,
                    new ConversationSummary.ConversationSummaryRecentMessageTimeSorter());

            if (request.future) {
                conversationList.remove(conversationList.size() - 1);
            }

            int fetchSize = conversationList.size() > request.size ? request.size
                    : conversationList.size();

            conversationList = request.future ? conversationList.subList(conversationList.size()
                    - fetchSize, conversationList.size()) : conversationList.subList(0, fetchSize);

        }
        GetConversationSummariesRes response = new GetConversationSummariesRes();
        response.list.addAll(conversationList);
        response.totalHits = userMailBoxInfo.conversationCount;
        return response;
    }

    public List<ConversationSummary> getConversationSummaries(String userId, int count)
            throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);
        StringBuilder builder = new StringBuilder();
        builder.append(userId).append("_");
        List<ConversationSummary> conversationList;
        try {
            conversationList = userConversationTableWrapper.getDataWithTimeRange(
                    builder.toString(), "conversations", "data", count, userId, false, 0,
                    System.currentTimeMillis());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, e);
        }

        return conversationList;
    }

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

        HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);

        Logger.debug(" Updating mail box for " + userId);
        List<ConversationSummary> conversationList;
        UserMailBoxInfo info = UserMailBoxInfoDAO.INSTANCE.getByUserId(userId);
        StringBuilder builder = new StringBuilder();
        builder.append(userId).append("_");
        String key = builder.toString();
        info.unreadConversationCount = 0;
        info.conversationCount = 0;
        do {

            conversationList = userConversationTableWrapper.getData(key, "conversations", "data",
                    MAXIMUM_BATCH_COUNT, userId);

            if (CollectionUtils.isNotEmpty(conversationList)) {
                for (ConversationSummary conversationSummary : conversationList) {
                    LOGGER.debug(" userId " + userId + " Current conversation summary "
                            + conversationSummary.getStatus());
                    if (conversationSummary.getStatus() == ConversationStatus.UNREAD) {
                        info.incrementUnread();
                    }
                    info.incrementConversationCount();
                    key = conversationSummary.getKey();
                }
            }

        } while (CollectionUtils.isNotEmpty(conversationList)
                && conversationList.size() == MAXIMUM_BATCH_COUNT);

        UserMailBoxInfoDAO.INSTANCE.save(info);

        return true;
    }

    public List<ConversationSummary> getMoreConversationSummaries(String userId, long timestamp,
            int count, boolean future) throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);
        long minTimeStamp = 0;
        long maxTimeStamp = System.currentTimeMillis();
        if (future) {
            minTimeStamp = timestamp;
        } else {
            maxTimeStamp = timestamp;
        }
        List<ConversationSummary> conversationList = null;
        try {

            conversationList = userConversationTableWrapper.getDataWithTimeRange(userId,
                    "conversations", "data", count, userId, false, minTimeStamp, maxTimeStamp);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, e);
        }

        return conversationList;

    }

    public GetConversationSummaryRes getConversationSummary(GetConversationSummaryReq request)
            throws VedantuException {

        ConversationSummary conversationSummary = getConversationSummary(request.userId,
                request.userConversationId);
        conversationSummary.setMostRecentSender(MessageUtil.populateUserNewsEntityDetails(
                request.orgId, conversationSummary.getMostRecentSender()));
        annotateImages(conversationSummary);
        GetConversationSummaryRes response = new GetConversationSummaryRes();
        response.summary = conversationSummary;
        return response;
    }

    public ConversationSummary getConversationSummary(String userId, String userConversationId)
            throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);
        LOGGER.info("Conversation summary for userConversationId: " + userConversationId);
        ConversationSummary conversationSummary = userConversationTableWrapper.getExact(
                userConversationId, "conversations", "data");

        return conversationSummary;
    }

    public GetMessageRes getMessage(GetMessageReq request) throws VedantuException {

        Message message = getMessage(request.userId, request.messageId);

        LOGGER.info("Message: " + message.getSubject());
        LOGGER.info("Decorating sender: " + message.getSender() + " Receivers"
                + message.getSender());
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
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES),
                Message.class);
        Message message = messageTableWrapper.getExact(messageId, "messages", "data");

        return message;

    }

    List<MessageSummary> getMessageSummaries(@Required String userId,
            @Required String conversationId, int count) throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);
        StringBuilder builder = new StringBuilder();
        builder.append(userId).append("_")
                .append(String.format("%020d", Long.MAX_VALUE - System.currentTimeMillis()))
                .append("_").append(conversationId).append("_");

        StringBuilder regexBuilder = new StringBuilder();

        regexBuilder.append("^(").append(userId).append(").*(?=(").append(conversationId)
                .append(")).*");

        List<MessageSummary> messageSummaryList = userMessageWrapper.getDataRegex(
                builder.toString(), "messages", "data", count, regexBuilder.toString());
        return messageSummaryList;
    }

    public MessageSummary getMessageSummary(@Required String orgId, @Required String userId,
            @Required String userMessageId) throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);
        MessageSummary summary = userMessageWrapper.getExact(userMessageId, "messages", "data");

        summary.setSender(MessageUtil.populateUserNewsEntityDetails(orgId, summary.getSender()));
        annotateImages(summary);
        return summary;
    }

    public GetConversationRes getConversation(GetConversationReq request) throws VedantuException {

        Conversation conversation = getConversation(request.userId, request.conversationId);
        conversation.setParticipants(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                conversation.getParticipants()));
        GetConversationRes reservationRes = new GetConversationRes();
        reservationRes.conversation = conversation;

        return reservationRes;
    }

    public Conversation getConversation(@Required String userId, @Required String conversationId)
            throws VedantuException {

        HbaseTableWrapper<Conversation> userConversationWrapper = new HbaseTableWrapper<Conversation>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_CONVERSATION),
                Conversation.class);
        Conversation conversation = userConversationWrapper.getExact(conversationId,
                "conversations", "data");

        return conversation;
    }

    public GetConversationUsersRes getConversationUsers(GetConversationUsersReq request)
            throws VedantuException {

        GetConversationUsersRes response = new GetConversationUsersRes();
        Conversation conversation = getConversation(request.userId, request.conversationId);

        List<SrcEntity> excludes = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(request.excludeUserIds)) {
            for (String userId : request.excludeUserIds) {
                SrcEntity excludeEntity = new SrcEntity(EntityType.USER, userId);
                excludes.add(excludeEntity);
            }
        }

        if (conversation == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_CONVERSATION_ID);
        }
        if (CollectionUtils.isNotEmpty(conversation.getParticipants())) {
            LOGGER.debug(" NUmber of participants in this list " + conversation.getParticipants());
            List<SrcEntity> list = new ArrayList<SrcEntity>();
            list.addAll(conversation.getParticipants());
            list.removeAll(excludes);
            Collections.sort(list);
            request.start = request.start < 0 || request.start >= list.size() ? 0 : request.start;
            request.size = request.size < 0 ? 0 : (request.size > list.size() ? list.size()
                    : request.size);

            List<SrcEntity> requestedUserList = list.subList(request.start, request.size);
            if (CollectionUtils.isNotEmpty(requestedUserList)) {
                LOGGER.debug(" Populating users now ");
                response.list.addAll(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        requestedUserList));

                response.totalHits = conversation.getParticipants().size();
            }

            return response;
        }
        LOGGER.debug(" No  of participants in this list ");
        return null;

    }

    public GetMessageSummariesRes getMessageSummaries(GetMessageSummariesReq request)
            throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);

        if (totalHits != null) {
            // TODO make it async
            Conversation conversation = getConversation(request.userId, request.conversationId);
            totalHits.setValue(conversation.getMessageCount());
        }

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
                summary.setSender(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        summary.getSender()));
                summary.setReceiver(MessageUtil.populateUserNewsEntityDetails(request.orgId,
                        summary.getReceiver()));
                annotateImages(summary);
                LOGGER.info("Content" + summary.getContent());
            }
        }

        GetMessageSummariesRes response = new GetMessageSummariesRes();
        response.totalHits = totalHits.longValue();
        response.list.addAll(messageSummaryList);
        return response;
    }

    public List<MessageSummary> getMessageSummaries(@Required String userId,
            @Required String userMessageId, @Required String conversationId, int count)
            throws VedantuException {

        HbaseTableWrapper<MessageSummary> userMessageTableWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);

        StringBuilder regexBuilder = new StringBuilder();

        regexBuilder.append("^(").append(userId).append(").*(?=(").append(conversationId)
                .append(")).*");
        List<MessageSummary> messageSummaryList = userMessageTableWrapper.getDataRegex(
                userMessageId, "messages", "data", count, regexBuilder.toString());

        return messageSummaryList;

    }

    /**
     * This will work as FB like messages.
     * 
     * @param userId
     * @param userMessageId
     * @return
     * @throws VedantuException
     */
    public boolean markAsRead(@Required String userId, @Required String userMessageId)
            throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);
        MessageSummary messageSummary = userMessageWrapper.getExact(userMessageId, "messages",
                "data");
        messageSummary.setStatus(ConversationStatus.READ);
        userMessageWrapper.addData(userMessageId, "messages", "data", messageSummary);
        ConversationSummary conversationSummary = userConversationWrapper.getExact(
                getUserConversationId(new SrcEntity(EntityType.USER, userId),
                        messageSummary.getConversationId()), "conversations", "data");

        conversationSummary.setStatus(ConversationStatus.READ);
        userConversationWrapper.addData(userMessageId, "conversations", "data", messageSummary);
        // TODO see if all messages in conversation mark read
        // mark message as read and update conversation summary by
        return true;
    }

    /**
     * This will work as FB like messages.
     * 
     * @param userId
     * @param userMessageId
     * @return
     * @throws VedantuException
     */
    public boolean markAsUnread(@Required String userId, @Required String userMessageId)
            throws VedantuException {

        HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);
        MessageSummary messageSummary = userMessageWrapper.getExact(userMessageId, "messages",
                "data");
        messageSummary.setStatus(ConversationStatus.UNREAD);
        userMessageWrapper.addData(userMessageId, "messages", "data", messageSummary);
        ConversationSummary conversationSummary = userConversationWrapper.getExact(
                getUserConversationId(new SrcEntity(EntityType.USER, userId),
                        messageSummary.getConversationId()), "conversations", "data");

        conversationSummary.setStatus(ConversationStatus.UNREAD);
        userConversationWrapper.addData(userMessageId, "conversations", "data", messageSummary);
        // TODO see if all messages in conversation mark read
        // mark message as read and update conversation summary by
        return true;
    }

    public GetUserMailBoxInfoRes getUserMailBoxInfo(GetUserMailBoxInfoReq request)
            throws VedantuException {

        UserMailBoxInfo userMailBoxInfo = UserMailBoxInfoDAO.INSTANCE.getByUserId(request.userId);

        if (userMailBoxInfo == null) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }
        GetUserMailBoxInfoRes response = new GetUserMailBoxInfoRes(userMailBoxInfo);
        return response;
    }

    public MarkConversationRes markConversation(MarkConversationReq request)
            throws VedantuException {

        boolean success = markConversation(request.userId, request.userConversationId,
                request.status);

        return new MarkConversationRes(success, request.userConversationId);

    }

    private boolean markConversation(@Required String userId, @Required String userConversationId,
            @Required ConversationStatus newStatus) throws VedantuException {

        LOGGER.debug("Marking conversation " + userConversationId + " status " + newStatus);
        HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                Play.application().configuration()
                        .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                ConversationSummary.class);

        HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                Play.application().configuration().getString(MessageUtil.TABLE_NAME_USER_MESSAGE),
                MessageSummary.class);

        ConversationSummary conversationSummary = userConversationWrapper.getExact(
                userConversationId, "conversations", "data");
        LOGGER.debug("Current  conversation " + userConversationId + " status "
                + conversationSummary.getStatus());
        if (conversationSummary.getStatus() == newStatus) {
            LOGGER.debug("No status update as  both requested and existing status is same"
                    + userConversationId + " status " + conversationSummary.getStatus());
            return true;
        }

        DBObject userMailBoxQuery = new BasicDBObject();
        userMailBoxQuery.put(ConstantsGlobal.USER_ID, userId);
        UserMailBoxInfo userMailBoxInfo = UserMailBoxInfoDAO.INSTANCE.getByUserId(userId);

        if (conversationSummary != null) {
            int MAXIMUM_FETCH_COUNT = 30;
            List<MessageSummary> messageSummaries = null;

            do {

                messageSummaries = getMessageSummaries(userId,
                        conversationSummary.getConversationId(), MAXIMUM_FETCH_COUNT);
                LOGGER.debug(" FOund message count " + messageSummaries.size());
                for (MessageSummary summary : messageSummaries) {
                    summary.setStatus(newStatus);
                    userMessageWrapper.addData(summary.getKey(), "messages", "data", summary);
                    LOGGER.debug(" New status will be " + newStatus);
                    if (newStatus == ConversationStatus.UNREAD) {
                        conversationSummary.incrementMessageUnread();
                    } else {
                        conversationSummary.decrementMessageUnread();
                    }
                }

            } while (CollectionUtils.isNotEmpty(messageSummaries)
                    && messageSummaries.size() >= MAXIMUM_FETCH_COUNT);
            conversationSummary.setStatus(newStatus);
            userConversationWrapper.addData(conversationSummary.getUserConversationId(),
                    "conversations", "data", conversationSummary);
            if (conversationSummary.getStatus() == ConversationStatus.UNREAD) {
                userMailBoxInfo.incrementUnread();
            } else {
                userMailBoxInfo.decrementUnread();
            }
            UserMailBoxInfoDAO.INSTANCE.save(userMailBoxInfo);
        }
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
            HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                    Play.application().configuration()
                            .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                    ConversationSummary.class);
            // delete all other messages

            UserMailBoxInfo userMailBoxInfo = UserMailBoxInfoDAO.INSTANCE.getByUserId(userId);

            ConversationSummary conversationSummary = userConversationWrapper.getExact(
                    userConversationId, "conversations", "data");

            if (userConversationWrapper.delete(userConversationId)) {
                if (conversationSummary.getStatus() == ConversationStatus.UNREAD) {
                    userMailBoxInfo.decrementConversationCount();
                    userMailBoxInfo.decrementUnread();
                }
                UserMailBoxInfoDAO.INSTANCE.save(userMailBoxInfo);
            }

        }
        return false;
    }

    public boolean deleteUserMessage(@Required String userId, @Required String userMessageId)
            throws VedantuException {

        if (userMessageId.startsWith(userId)) {
            HbaseTableWrapper<MessageSummary> userMessageWrapper = new HbaseTableWrapper<MessageSummary>(
                    Play.application().configuration()
                            .getString(MessageUtil.TABLE_NAME_USER_MESSAGE), MessageSummary.class);

            HbaseTableWrapper<ConversationSummary> userConversationWrapper = new HbaseTableWrapper<ConversationSummary>(
                    Play.application().configuration()
                            .getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
                    ConversationSummary.class);

            MessageSummary messageSummary = userMessageWrapper.getExact(userMessageId, "messages",
                    "data");

            boolean deleteStatus = userMessageWrapper.delete(userMessageId);

            if (deleteStatus) {
                ConversationSummary conversationSummary = userConversationWrapper.getExact(
                        getUserConversationId(new SrcEntity(EntityType.USER, userId),
                                messageSummary.getConversationId()), "conversations", "data");

                StringBuilder regexBuilder = new StringBuilder();
                regexBuilder.append(userId).append("_").append(".*").append("_")
                        .append(conversationSummary.getConversationId()).append("_");

                MessageSummary nextMessageSummary = userMessageWrapper.getNextData(userMessageId,
                        "messages", "data", regexBuilder.toString());

                if (nextMessageSummary == null) {
                    deleteUserConversation(userId, conversationSummary.getConversationId());
                } else {
                    conversationSummary.setContent(nextMessageSummary.getContent());
                    userConversationWrapper.addData(conversationSummary.getUserConversationId(),
                            "conversations", "data", conversationSummary);
                }

            }
        }
        return false;
    }

    // public static void main(String[] args) throws MasterNotRunningException,
    // ZooKeeperConnectionException, VedantuException, SecurityException,
    // NoSuchFieldException {
    // Configuration config = new Configuration();
    // config.addResource(new String(
    // "/home/vikram/Documents/required/hbase-0.92.0/conf/hbase-site.xml"));
    // // config.addResourc e(new
    // // Path("/usr/local/hbase-0.92.0/conf/hbase-site.xml"));
    //
    // // TODO: find the config file from where these hbase properties can be
    // // read and add that resource
    // config.set("hbase.zookeeper.quorum", "localhost");
    // // config.set("hbase.zookeeper.quorum", "localhost");
    // config.set("hbase.zookeeper.property.clientPort", "2181");
    // // config.set("hbase.zookeeper.property.clientPort", "2181");
    //
    // HBaseAdmin admin = new HBaseAdmin(config);
    // HTablePool hTablePool = new HTablePool(config, 1000);
    // Message testMessage = new Message();
    // testMessage.setMessageId("testId");
    // HbaseTableWrapper<Message> messageWrapper = new
    // HbaseTableWrapper<Message>(
    // "message_table", Message.class);
    // testMessage.setContent("testContent");
    // messageWrapper.addData("key_2", "messages", "data", testMessage);
    // List<Message> newMessage = messageWrapper.getData("key", "messages",
    // "data", 10, "key");
    // System.out.println(newMessage.get(0).getContent());
    // System.out.println(messageWrapper.doesExist("key"));
    // messageWrapper.delete("key");
    // System.out.println(messageWrapper.doesExist("key"));
    //
    // // Message message = new Message();
    // // message.setActionOnParentMessage("REPLY");
    // // message.setSenderId("user1");
    // // List<String> idList = new ArrayList<String>();
    // // idList.add("user2");
    // // message.setReceiverIdList(idList);
    // MessageManager manager = new MessageManager();
    // // manager.tester();
    // // manager.sendMessage("user1", message);
    // //
    // }

    private void annotateImages(IReverseImageMapperProcessor content) {

        content.addImageSrcUrl();
    }

}
