package com.vedantu.eventbus.processors.comm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.comm.daos.ConversationDAO;
import com.vedantu.comm.daos.ConversationSummaryDAO;
import com.vedantu.comm.daos.UserMailBoxInfoDAO;
import com.vedantu.comm.email.details.MessageEmailDetails;
import com.vedantu.comm.enums.ConversationStatus;
import com.vedantu.comm.event.details.MessageDistributeDetails;
import com.vedantu.comm.models.hbase.messages.Message;
import com.vedantu.comm.models.hbase.messages.MessageSummary;
import com.vedantu.comm.models.mongo.Conversation;
import com.vedantu.comm.models.mongo.ConversationSummary;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.comm.utils.MessageUtil;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.eventbus.emails.notification.generators.EmailNotificationGenerator;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.pojos.UserEmailInfo;

public class MessageDistributeProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(MessageDistributeProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        if (event.action == EventActionType.REMOVE) {
            LOGGER.info("as event action is remove hence not processing the event for newsActivity generation : "
                    + consumable._getConsumableId());
            return Status.SUCCESS;
        }
        LOGGER.info("processing Event for " + event.getType() + " process for userId :"
                + event.getUserId());

        LOGGER.info("fetching eventDetails");
        MessageDistributeDetails details = (MessageDistributeDetails) event.fetchEventDetails();

        if (details == null) {
            LOGGER.error(" Invalid event details " + event.getType());
            return Status.FAILURE;
        }
        if (StringUtils.isEmpty(details.conversationId) || StringUtils.isEmpty(details.messageId)) {
            LOGGER.error("Message delivery failed as not conversationId or messageId specified"
                    + details);
            return Status.FAILURE;
        }

        HbaseTableWrapper<Message> messageTableWrapper = new HbaseTableWrapper<Message>(Play
                .application().configuration().getString(MessageUtil.TABLE_NAME_MESSAGES_V2),
                Message.class);
        try {
            Message message = messageTableWrapper.getExact(details.messageId, "messages", "data");
            if (message == null) {
                LOGGER.debug("Message not found for message Id " + details.messageId);
                return Status.FAILURE;
            }

            if (StringUtils.isEmpty(message.getConversationId())) {
                LOGGER.debug("Conversation is not set in message with message Id "
                        + details.messageId);
                return Status.FAILURE;
            }

            Conversation conversation = ConversationDAO.INSTANCE.getById(
                    message.getConversationId(), VedantuRecordState.ACTIVE);

            if (conversation == null) {
                LOGGER.debug("Conversation is not found for message Id " + details.messageId);
                return Status.FAILURE;
            }

            List<String> userIds = new ArrayList<String>();
            for (SrcEntity recepient : message.receivers) {
                if (recepient.type == EntityType.USER) {
                    userIds.add(recepient.id);
                }
            }
            Map<String, UserEmailInfo> receiverEmailInfoMap = EmailNotificationGenerator
                    .collectUserEmailInfo(userIds, message.orgId);

            MessageEmailDetails messageEmailDetails = new MessageEmailDetails();
            userIds.clear();
            userIds.add(message.sender.id);
            Map<String, UserEmailInfo> senderMap = EmailNotificationGenerator.collectUserEmailInfo(
                    userIds, message.orgId, true);
            messageEmailDetails.senderInfo = senderMap.get(message.sender.id);
            messageEmailDetails.orgId = message.orgId;
            LOGGER.debug("Sender Info" + messageEmailDetails.senderInfo + " after intialization ");

            if (StringUtils.isEmpty(message.orgId)) {

                Organization org = OrganizationDAO.INSTANCE.getById(message.orgId);
                messageEmailDetails.organizationName = org.name;

            }
            HbaseTableWrapper<MessageSummary> userMessageTableWrapper = new HbaseTableWrapper<MessageSummary>(
                    Play.application().configuration()
                            .getString(MessageUtil.TABLE_NAME_USER_MESSAGE_V2),
                    MessageSummary.class);
            LOGGER.debug("\n Message Receivers" + message.getReceivers() + " \n Conversation id "
                    + message.getConversationId());

            Set<SrcEntity> forwardList = new HashSet<SrcEntity>();
            forwardList.addAll(message.getReceivers());

            MessageSummary messageSummary = new MessageSummary(message.getMessageId());
            messageSummary.setMessageId(message.messageId);
            messageSummary.setSender(message.getSender());
            messageSummary.setSentTime(message.getSentOnTimestamp());
            messageSummary.setConversationId(message.conversationId); // mandatory
            messageSummary.setStatus(ConversationStatus.UNREAD);
            messageSummary.setContent(message.getContent());

            LOGGER.info(" List" + forwardList + " receievd list" + message.getReceivers() + " "
                    + message.getSender());

            messageEmailDetails.addHeader(MessageEmailDetails.X_CONVERSATION_ID,
                    conversation._getStringId());
            messageEmailDetails.addHeader(MessageEmailDetails.X_MESSAGE_ID, message.getKey());

            for (SrcEntity receiver : forwardList) {

                messageSummary.setReceiver(receiver);
                messageSummary.setReceivedTime(System.currentTimeMillis());

                // this will upsert conversation summary if doesnt exist
                // this always create or reinitialize conversationSummary
                ConversationSummary olderConversationSummary = ConversationSummaryDAO.INSTANCE
                        .update(messageSummary.conversationId, receiver.id, false,
                                conversation.firstMesssageId, conversation.participants.size(),
                                conversation.orgId, messageSummary.getSentTime());
                boolean isNewConversation = false;
                // olderConversationSummary doesn't exist or existed and was deleted before
                if (olderConversationSummary == null
                        || olderConversationSummary.recordState == VedantuRecordState.DELETED) {
                    isNewConversation = true;

                }

                LOGGER.debug(" Saving user messages with key" + messageSummary.getNewKey());
                userMessageTableWrapper.addData(messageSummary.getNewKey(), "messages", "data",
                        messageSummary);

                ConversationSummaryDAO.INSTANCE.updateMessageSummary(messageSummary.conversationId,
                        receiver.id, messageSummary.content, message.getSubject(),
                        messageSummary.getSentTime(), messageSummary.sender.id,
                        messageSummary.messageId);

                // TODO this part will send out emails for receivers it will need more
                // decorations though
                if (!receiver.equals(message.getSender())) {
                    UserEmailInfo user = receiverEmailInfoMap.get(receiver.id);
                    if (user != null && user.isEmailVerified) {
                        messageSummary.addImageSrcUrl();

                        messageEmailDetails.user = user;
                        messageEmailDetails.user.setCategory(MailCategory.NOTIFICATION);
                        messageEmailDetails.messageContent = messageSummary.content;
                        messageEmailDetails.setSubject(conversation.subject);
                        messageEmailDetails.userConversationId = receiver.id + "_"
                                + conversation._getStringId();
                        messageEmailDetails.addRecepient(user.getFullName(), user.email);
                        messageEmailDetails.addHeader(MessageEmailDetails.X_USER_CONVERSATION_ID,
                                conversation._getStringId());
                        messageEmailDetails.addHeader(MessageEmailDetails.X_USER_MESSAGE_ID,
                                messageSummary.messageId);

                        LOGGER.debug("Sender Info" + messageEmailDetails.senderInfo
                                + " before cloning ");
                        AbstractContentManager.generateEventAysc(message.sender.id,
                                messageEmailDetails.clone(), EventType.SEND_EMAIL);
                        messageEmailDetails.resetRecepients();

                    }
                }

                UserMailBoxInfoDAO.INSTANCE
                        .updateCounts(receiver.id,
                        // if receiver is sender of this only in case of resetting deleted
                        // state to active
                                (isNewConversation && conversation.userId.equals(receiver.id)),
                                // check if newConversations or oldConversation was read before
                                (isNewConversation || olderConversationSummary.status == ConversationStatus.READ),
                                // update only if new conversation
                                isNewConversation);
                // send out and notification for users here

            }
        } catch (VedantuException ex) {
            LOGGER.error(" Message distribution failed", ex);
            return Status.FAILURE;
        } catch (CloneNotSupportedException e) {
            LOGGER.error(" Message distribution failed", e);
            return Status.FAILURE;
        }

        return Status.SUCCESS;
    }
}
