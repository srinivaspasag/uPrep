package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.ConversationStatus;
import com.lms.enums.MessageAction;
import com.lms.enums.NumberUpdate;
import com.lms.event.details.MessageDistributeDetails;
import com.lms.managers.AbstractContentManager;
import com.lms.models.Conversation;
import com.lms.models.ConversationRes;
import com.lms.models.ConversationSummary;
import com.lms.models.UserMailBoxInfo;
import com.lms.models.messages.Message;
import com.lms.models.messages.MessageSummary;
import com.lms.pojos.AddedMember;
import com.lms.pojos.GetMessageReq;
import com.lms.pojos.requests.GetConversationReq;
import com.lms.pojos.requests.GetUserMailBoxInfoReq;
import com.lms.pojos.requests.messages.*;
import com.lms.pojos.response.*;
import com.lms.pojos.response.messages.*;
import com.lms.repository.ConversationRepo;
import com.lms.repository.ConversationSummaryRepo;
import com.lms.repository.MessageSummaryRepo;
import com.lms.repository.UserMailBoxInfoRepo;
import com.lms.requests.GetConversationUsersReq;
import com.lms.requests.GetMessageSummariesReq;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MessageManager extends AbstractContentManager {
	private final static Logger logger = LoggerFactory.getLogger(MessageManager.class);
	private static final int MAXIMUM_BATCH_COUNT = 30;
	@Autowired
	private ConversationSummaryRepo conversationSummaryRepo;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private MessageSummaryRepo messageSummaryRepo;
	@Autowired
	private ConversationRepo conversationRepo;
	private final int USER_CONVERSAION_USER_ID_INDEX = 0;
	private final int USER_CONVERSAION_CONVERSATION_ID_INDEX = 1;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private UserMailBoxInfoRepo userMailBoxInfoRepo;


	public GetConversationSummaryRes getConversationSummary(GetConversationSummaryReq request) {

		String[] splittedUserConversation = request.userConversationId.split("_");

		ConversationSummary mongoSummary = find(splittedUserConversation[1], splittedUserConversation[0]);

		List<ConversationSummary> mongoSummaries = new ArrayList<ConversationSummary>();
		mongoSummaries.add(mongoSummary);

		List<com.lms.models.messages.ConversationSummary> conversationSummaries = getConversationSummary(
				request.orgId, mongoSummaries);
		com.lms.models.messages.ConversationSummary hbaseConSummary = conversationSummaries
				.get(0);

		GetConversationSummaryRes response = new GetConversationSummaryRes();
		response.summary = hbaseConSummary;
		return response;
	}

	public ConversationSummary find(String conversationId, String userId) {

		return conversationSummaryRepo.findByConversationIdAndUserId(conversationId, userId);

	}

	public Conversation findMessageSummary(String conversationId, String userMessageId, String userId) {
		if (conversationId == null) {
			return conversationRepo.findByUserIdAndFirstMesssageId(userId.trim(), userMessageId.toLowerCase().trim());
		} else {
			return conversationRepo.findByUserIdAndConversationId(userId, conversationId);
		}

	}

	public Conversation findConversation(String conversationId, String userId) {

		return conversationRepo.findByIdAndUserId(conversationId, userId);

	}


	public List<com.lms.models.messages.ConversationSummary> getConversationSummary(String orgId,
																					List<ConversationSummary> mongoSummaries) {

		List<com.lms.models.messages.ConversationSummary> conversationSummaries = new ArrayList<com.lms.models.messages.ConversationSummary>();
		for (ConversationSummary mongoSummary : mongoSummaries) {
			com.lms.models.messages.ConversationSummary conversationSummary = new com.lms.models.messages.ConversationSummary();

			conversationSummary.content = mongoSummary.content;
			conversationSummary.subject = mongoSummary.subject;
			conversationSummary.firstMessageId = mongoSummary.firstMessageId;
			conversationSummary.mostRecentMessageTiming = mongoSummary.mostRecentMessageTime;
			conversationSummary.mostRecentSender = new SrcEntity(EntityType.USER, mongoSummary.mostRecentSenderId);
			conversationSummary.messageCount = mongoSummary.messageCount;
			conversationSummary.messagesUnread = mongoSummary.messagesUnread;
			conversationSummary.numOfParticipants = mongoSummary.numOfParticipants;
			conversationSummary.orgId = mongoSummary.orgId;
			conversationSummary.userConversationId = getUserConversationId(
					new SrcEntity(EntityType.USER, mongoSummary.userId), mongoSummary.conversationId);
			conversationSummary.conversationId = mongoSummary.conversationId;

			conversationSummary.setStatus(mongoSummary.status);

			conversationSummary.setTimestamp(conversationSummary.mostRecentMessageTiming);

			conversationSummary.setMostRecentSender(
					messageUtil.populateUserNewsEntityDetails(orgId, conversationSummary.getMostRecentSender()));
			annotateImages(conversationSummary);

			conversationSummaries.add(conversationSummary);

		}
		return conversationSummaries;
	}

	private String getUserConversationId(SrcEntity user, String conversationId) {

		StringBuilder builder = new StringBuilder();
		builder.append(user.id).append("_").append(conversationId);
		return builder.toString();
	}

	private String getUserMessageId(SrcEntity user, String userMessageId) {

		StringBuilder builder = new StringBuilder();
		builder.append(user.id).append("_").append(userMessageId);
		return builder.toString();
	}

	public List<com.lms.models.messages.MessageSummary> getMessageSummary(String orgId,
																		  List<MessageSummary> mongoSummaries) {

		List<com.lms.models.messages.MessageSummary> messageSummaries = new ArrayList<MessageSummary>();
		for (MessageSummary mongoSummary : mongoSummaries) {
			com.lms.models.messages.MessageSummary messageSummary = new com.lms.models.messages.MessageSummary(mongoSummary.messageId);

			messageSummary.content = mongoSummary.content;
			messageSummary.conversationId = mongoSummary.conversationId;
			messageSummary.parentMessageId = mongoSummary.parentMessageId;
			messageSummary.status = mongoSummary.status;
			if (messageSummary != null && messageSummary.sender.getType() != null && messageSummary.sender.id != null)
				messageSummary.sender = new SrcEntity(mongoSummary.sender.type, mongoSummary.sender.id);
			messageSummary.types = mongoSummary.types;
			messageSummary.userMessageId = mongoSummary.userMessageId;
			//messageSummary.numOfParticipants = mongoSummary.numOfParticipants;
			messageSummary.orgId = mongoSummary.orgId;
			messageSummary.messageId = getUserMessageId(
					new SrcEntity(EntityType.USER, mongoSummary.userId), mongoSummary.userMessageId);
			messageSummary.conversationId = mongoSummary.conversationId;

			messageSummary.setStatus(mongoSummary.status);

			//messageSummary.setTimestamp(messageSummary.getTimestamp());

			//messageSummary.setMostRecentSender(messageUtil.populateUserNewsEntityDetails(orgId, messageSummary.getMostRecentSender()));
			annotateImages(messageSummary);

			messageSummaries.add(messageSummary);

		}
		return messageSummaries;
	}


	private void annotateImages(com.lms.models.messages.ConversationSummary content) {

		addImageSrcUrl(EntityType.MESSAGE, content.content);
	}

	public GetConversationSummariesRes getConversationSummaries(GetConversationSummariesReq request) {
		AtomicLong totalHits = new AtomicLong(0);
		List<ConversationSummary> summaries = get(request.userId,
				request.start, request.size, totalHits, VedantuRecordState.ACTIVE);

		List<com.lms.models.messages.ConversationSummary> conversationSummaries = getConversationSummary(
				request.orgId, summaries);

		GetConversationSummariesRes response = new GetConversationSummariesRes();
		response.list.addAll(conversationSummaries);
		response.totalHits = totalHits.longValue();

		return response;

	}

	public List<ConversationSummary> get(String userId, int start, int size, AtomicLong totalHits,
										 VedantuRecordState state) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("userId").is(userId);
		criteria.and("recordState").is(state);
		totalHits.set(mongoTemplate.count(query, ConversationSummary.class));
		query.skip(start).limit(size).with(Sort.by(Sort.Direction.DESC, "mostRecentMessageTime"));
		List<ConversationSummary> summaries = mongoTemplate.find(query, ConversationSummary.class);
		return summaries;

	}


	private void annotateImages(com.lms.models.messages.MessageSummary content) {

		addImageSrcUrl(EntityType.MESSAGE, content.content);
	}

	private void annotateImages(MessageRes content) {

		addImageSrcUrl(EntityType.MESSAGE, content.content);
	}


	public MessageSummary getMessageSummary(String orgId, String userId, String userMessageId) throws VedantuException {


		Conversation mongoSummary = findMessageSummary(null, userMessageId, userId);

		List<Conversation> mongoSummaries = new ArrayList<>();
		mongoSummaries.add(mongoSummary);

		List<com.lms.models.messages.MessageSummary> messageSummaries = getMessageSum(
				orgId, mongoSummaries);
		com.lms.models.messages.MessageSummary hbaseConSummary = messageSummaries
				.get(0);


		return hbaseConSummary;
	}

	public GetMessageSummariesRes getMessageSummaries(GetMessageSummariesReq request)
			throws VedantuException {

		AtomicLong totalHits = new AtomicLong(0L);

		if (totalHits != null) {
			// TODO make it async
			ConversationRes conversation = getConversation(request.userId, request.conversationId);
			totalHits.set(conversation.getMessageCount());
		}

		List<MessageSummary> messageSummaryList = null;
		if (!StringUtils.isEmpty(request.userMessageId)) {
			messageSummaryList = getMessageSummaries(request.userId, request.userMessageId, request.conversationId, request.size);
		} else {
			messageSummaryList = getMessageSummaries(request.userId, request.conversationId, request.size);
		}
		if (CollectionUtils.isNotEmpty(messageSummaryList)) {

			for (MessageSummary summary : messageSummaryList) {
				summary.setSender(messageUtil.populateUserNewsEntityDetails(request.orgId,
						summary.getSender()));
				summary.setReceiver(messageUtil.populateUserNewsEntityDetails(request.orgId,
						summary.getReceiver()));
				annotateImages(summary);
				logger.info("Content" + summary.getContent());
			}
		}

		GetMessageSummariesRes response = new GetMessageSummariesRes();
		response.totalHits = totalHits.longValue();
		response.list.addAll(messageSummaryList);
		return response;
	}

	public List<MessageSummary> getMessageSummaries(@NotBlank(message = "userId should not be null") String userId, @NotBlank(message = "conversationId should not be null") String conversationId, int count) throws VedantuException {

		//String[] splittedUserConversation = conversationId.split("_");

		Conversation mongoSummary = findMessageSummary(conversationId, null, userId);

		List<Conversation> mongoSummaries = new ArrayList<Conversation>();
		mongoSummaries.add(mongoSummary);

		List<com.lms.models.messages.MessageSummary> messageSummaries = getMessageSum(userId, mongoSummaries);


		return messageSummaries;
	}

	public ConversationRes getConversation(@NotBlank(message = "userId should not be null") String userId, @NotBlank(message = "conversationId should not be null") String conversationId)
			throws VedantuException {

		/*HbaseTableWrapper<Conversation> userConversationWrapper = new HbaseTableWrapper<Conversation>(
				Play.application().configuration().getString(MessageUtil.TABLE_NAME_CONVERSATION),
				Conversation.class);
		Conversation conversation = userConversationWrapper.getExact(conversationId,
				"conversations", "data");*/

		//String[] splittedUserConversation = conversationId.split("_");

		Conversation mongoSummary = findConversation(conversationId, userId);
		//ConversationRes mongoSummary = getConversation(userId, conversationId);


		List<Conversation> mongoSummaries = new ArrayList<Conversation>();
		mongoSummaries.add(mongoSummary);

		List<ConversationRes> conversationSummaries = getConversation(userId, mongoSummaries);
		ConversationRes hbaseConSummary = conversationSummaries
				.get(0);
		return hbaseConSummary;
	}

	public List<ConversationRes> getConversation(String userId, List<Conversation> mongoSummaries) {

		List<ConversationRes> conversationSummaries = new ArrayList<ConversationRes>();
		for (Conversation mongoSummary : mongoSummaries) {
			ConversationRes conversationSummary = new ConversationRes();

			conversationSummary.setConversationId(mongoSummary.getConversationId());
			conversationSummary.setSubject(mongoSummary.getSubject());
			conversationSummary.setFirstMesssageId(mongoSummary.getFirstMesssageId());
			conversationSummary.setRecentMessageId(mongoSummary.getRecentMessageId());
			conversationSummary.setMessageCount(mongoSummary.getMessageCount());
			//conversationSummary.setParticipants(mongoSummary.getParticipants());
			conversationSummary.setLastUpdated(mongoSummary.getLastUpdated());
			conversationSummary.orgId = mongoSummary.orgId;
			conversationSummary.setConversationId(getUserConversationId(new SrcEntity(EntityType.USER, mongoSummary.getId().toString()), mongoSummary.getConversationId()));
			//conversationSummary.conversationId = mongoSummary.conversationId;

			conversationSummary.setSubject(mongoSummary.getSubject());

			conversationSummary.setTimeCreated(conversationSummary.getTimeCreated());


			//	conversationSummary.setMostRecentSender(messageUtil.populateUserNewsEntityDetails(orgId, conversationSummary.getMostRecentSender()));


			conversationSummaries.add(conversationSummary);

		}
		return conversationSummaries;

	}

	public List<MessageSummary> getMessageSummaries(@NotBlank(message = "userId should not be null") String userId, @NotBlank(message = "userMessageId should not be null") String userMessageId, @NotBlank(message = "conversationId should not be null") String conversationId, int count)
			throws VedantuException {

		String[] splittedUserConversation = userMessageId.split("_");

		Conversation conversation = findMessageSummary(splittedUserConversation[2], splittedUserConversation[1], splittedUserConversation[0]);

		List<Conversation> mongoSummaries = new ArrayList<Conversation>();
		mongoSummaries.add(conversation);

		List<com.lms.models.messages.MessageSummary> messageSummaries = getMessageSum(userId, mongoSummaries);


		return messageSummaries;

	}

	public GetMessageRes getMessage(GetMessageReq request) throws VedantuException {

		MessageRes message = getMessage(request.userId, request.messageId);

		logger.info("Message: " + message.getSubject());
		logger.info("Decorating sender: " + message.getSender() + " Receivers"
				+ message.getSender());
		message.setSender(messageUtil.populateUserNewsEntityDetails(request.orgId,
				message.getSender()));
		SrcEntity requester = new SrcEntity(EntityType.USER, request.userId);
		List<SrcEntity> receivers = message.getReceivers();
		receivers.remove(requester);

		List<SrcEntity> decoratedReceivers = messageUtil.populateUserNewsEntityDetails(
				request.orgId, receivers);
		message.setReceivers(decoratedReceivers);
		GetMessageRes response = new GetMessageRes();
		response.message = message;
		annotateImages(message);
		return response;
	}

	private MessageRes getMessage(@NotBlank(message = "userId should not be null") String userId, @NotBlank(message = "messageId should not be null") String messageId)
			throws VedantuException {

		ConversationSummary mongoSummary = find(userId, messageId);
		MessageRes messageSummaries = getMessageRes(userId, messageId, mongoSummary);
		return messageSummaries;


	}

	private MessageRes getMessageRes(String userId, String messageId, ConversationSummary mongoSummary) {
		MessageRes messageRes = new MessageRes();
		messageRes.setContent(mongoSummary.getContent());
		messageRes.setConversationId(mongoSummary.getConversationId());
		messageRes.setMessageId(mongoSummary.getFirstMessageId());
		messageRes.setOrgId(mongoSummary.getOrgId());
		messageRes.setSender(new SrcEntity(EntityType.USER, mongoSummary.mostRecentSenderId));

		return messageRes;
	}

	public GetUserMailBoxInfoRes getUserMailBoxInfo(GetUserMailBoxInfoReq request)
			throws VedantuException {

		UserMailBoxInfo userMailBoxInfo = getByUserId(request.userId);

		if (userMailBoxInfo == null) {
			throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
		}
		GetUserMailBoxInfoRes response = new GetUserMailBoxInfoRes(userMailBoxInfo);
		return response;
	}

	public UserMailBoxInfo getByUserId(String userId) {
		Query query = new Query();
		Criteria criteria = new Criteria();

		criteria.and("userId").is(userId);
		List<UserMailBoxInfo> updateOperations = mongoTemplate.find(query.addCriteria(criteria), UserMailBoxInfo.class);

		if (updateOperations != null) {
			logger.debug(" Updated messageUnread and messageCount");
			return updateOperations.get(0);
		}
		return null;

	}


	public SendMessageRes sendMessage(SendMessageReq request) {


		Message message = request.message;
		if (message.sender == null || !message.sender.id.equals(request.userId)) {
			throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
		}

		int retries = 0;
		SendMessageRes response = null;

		while (retries < 5) {
			try {

				if (StringUtils.isEmpty(message.orgId) && !StringUtils.isEmpty(request.orgId)) {
					message.orgId = request.orgId;

				}
				MessageAction action = message.getAction();
				String messageId = UUID.randomUUID().toString();
				message.setMessageId(messageId);
				message.removeImageSrc(true);
				message.setSentOnTimestamp(System.currentTimeMillis());
				message.setAction(action);

				Conversation conversation = null;
				if (!StringUtils.isEmpty(message.getConversationId())) {
					Optional<Conversation> conversationOptional = conversationRepo.findById(message.getConversationId());
					if (conversationOptional.isPresent()) {
						conversation = conversationOptional.get();

					}

				}
				if (conversation == null) {

					conversation = new Conversation();
					// conversation.setConversationId(conversationId);
					conversation.setSubject(message.subject);
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
					conversationRepo.save(conversation);

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
				//messageTableWrapper.addData(message.getKey(), "messages", "data", message);

				updateIncForNewMessage(conversation._getStringId());

				updateRecentMessageInfo(conversation._getStringId(),
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

				ConversationSummary olderConversationSummary = update(messageSummary.conversationId, message.getSender().id, true,
						conversation.firstMesssageId, conversation.participants.size(), conversation.orgId,
						messageSummary.getSentTime());

				boolean isNewConversation = false;
				if (olderConversationSummary == null
						|| olderConversationSummary.recordState == VedantuRecordState.DELETED) {
					isNewConversation = true;

				}
				updateMessageSummary(messageSummary.conversationId,
						messageSummary.userId, messageSummary.content, message.getSubject(),
						messageSummary.getSentTime(), messageSummary.sender.id,
						messageSummary.messageId);

				updateCounts(message.getSender().id,
						isNewConversation, false, isNewConversation);

				MessageDistributeDetails distributeDetails = new MessageDistributeDetails();
				distributeDetails.messageId = message.getKey();
				distributeDetails.conversationId = conversation._getStringId();
				generateEventAysc(messageSummary.sender.id, distributeDetails,
						EventType.MESSAGE_DISTRIBUTE);

				messageSummary.setSender(messageUtil.populateUserNewsEntityDetails(request.orgId,
						messageSummary.getSender()));
				annotateImages(messageSummary);
				response = new SendMessageRes(true, messageSummary);
				return response;
			} catch (VedantuException exception) {

				logger.error("Exception occured while sending message" + exception.getClass() + " "
						+ exception.getMessage(), exception);
				retries++;
			} catch (Exception exception) {
				logger.error("Exception occured while sending message" + exception.getClass() + " "
						+ exception.getMessage(), exception);
				retries++;
			}

		}
		throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED);


	}

	public void updateIncForNewMessage(String conversationId) {
		Conversation conversation = conversationRepo.findByConversationId(conversationId);
		conversation.setMessageCount(conversation.getMessageCount() + 1);
		conversationRepo.save(conversation);

	}

	public boolean updateRecentMessageInfo(String conversationId, long recentMessageTime, String recentMessageId) {

		logger.debug("Updateing conversation Id " + conversationId + " with most reced message time "
				+ recentMessageTime + " with recent message Id" + recentMessageId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("_id").is(conversationId);
		criteria.and("recentMessageTime").lte(recentMessageTime);
		Conversation conversation = mongoTemplate.findOne(query, Conversation.class);
		if (conversation != null) {
			conversation.setRecentMessageId(recentMessageId);
			conversation.setRecentMessageTime(recentMessageTime);
			conversationRepo.save(conversation);
			logger.debug(" Updated messageUnread and messageCount");
			return true;

		}

		logger.error("Update conversations failed for" + conversationId + " while updateing most recent message info ");
		return false;

	}

	public ConversationSummary update(String conversationId, String userId, boolean sender,
									  String firstMessageId, int participants, String orgId, long mostRecentMessageTime) {
		ConversationSummary conversationSummary = conversationSummaryRepo.findByConversationIdAndUserId(conversationId,
				userId);

		if (!sender) {
			conversationSummary.setMessagesUnread(conversationSummary.getMessagesUnread() + 1);
			conversationSummary.setStatus(ConversationStatus.UNREAD);

		} else {
			conversationSummary.setMessagesUnread(0);
			conversationSummary.setStatus(ConversationStatus.READ);

		}
		conversationSummary.setRecordState(VedantuRecordState.ACTIVE);
		conversationSummary.setMessageCount(conversationSummary.getMessageCount() + 1);
		conversationSummary.setMostRecentMessageTime(mostRecentMessageTime);
		conversationSummary.setFirstMessageId(firstMessageId);
		conversationSummary.setNumOfParticipants(participants);
		if (!StringUtils.isEmpty(orgId)) {
			conversationSummary.setOrgId(orgId);
		}

		// return find and modify and return correct count if
		ConversationSummary conversationSummaryResult = conversationSummaryRepo.save(conversationSummary);

		logger.debug("testing upsert and retriving new messages" + conversationSummaryResult);
		if (conversationSummaryResult != null) {
			logger.debug(" Updated messageUnread and messageCount");

		}
		return conversationSummaryResult;

	}

	public boolean updateMessageSummary(String conversationId, String userId,
										String currentMessageContent, String currentMessageSubject, long currentMessageTime,
										String currentMessageSenderId, String currentMessageId) {

		logger.debug("Updateing conversation Id " + conversationId + " for userId " + userId
				+ " with most reced message time " + currentMessageTime + " with recent message sender Id"
				+ currentMessageSenderId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("conversationId").is(conversationId);
		criteria.and("userId").is(userId);
		criteria.and("mostRecentMessageTime").lte(currentMessageTime);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		query.addCriteria(criteria);
		ConversationSummary conversationSummary = mongoTemplate.findOne(query, ConversationSummary.class);

		conversationSummary.setContent(currentMessageContent);
		if (!StringUtils.isEmpty(currentMessageSubject)) {
			conversationSummary.setSubject(currentMessageSubject);
		}

		conversationSummary.setMostRecentSenderId(currentMessageSenderId);
		conversationSummary.setMostRecentMessageId(currentMessageId);
		conversationSummary.setMostRecentMessageTime(currentMessageTime);

		// TODO we can do increment number of message count and unread message count but
		// as of now I am not but need to be evaluated what happens
		// assumption if this operation fails so I can update only counts as
		// conversationsummary
		// already counted for latest message

		ConversationSummary conversationSummaryResult = conversationSummaryRepo.save(conversationSummary);
		if (conversationSummaryResult != null) {
			logger.debug(" Updated messagesUnread and messageCount");
			return true;
		}
		logger.debug(" failed to updated conversation ");
		return false;
	}

	public boolean updateCounts(String userId, boolean sent, boolean unread, boolean total) {

		logger.debug("UserId " + userId + " sent " + sent + " unread " + unread
				+ " increment total " + total);
		Query query = new Query();
		Criteria criteria = new Criteria();
		Update updateOperations = new Update();

		criteria.and("userId").is(userId);
		query.addCriteria(criteria);
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

		UpdateResult updateResults = mongoTemplate.updateMulti(query, updateOperations, UserMailBoxInfo.class);

		if (updateResults != null) {
			logger.debug(" Updated messageUnread and messageCount");
			return true;
		}
		return false;

	}

	public UpdatedUserMailBoxInfoRes updateUsersMailBoxesInfos(List<String> userIds)
			throws VedantuException {

		long currentTime = System.currentTimeMillis();
		if (CollectionUtils.isNotEmpty(userIds)) {
			List<User> users = userRepo.findByIdIn(userIds);

			for (User user : users) {
				updateUserMailBoxInfo(user._getStringId());
			}
		} else {

			int currentBatchStart = 0;
			userIds = new ArrayList<String>();
			do {
				Set<String> userSet = getIdsByTime(0, currentTime,
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

	/*	HbaseTableWrapper<ConversationSummary> userConversationTableWrapper = new HbaseTableWrapper<ConversationSummary>(
				Play.application().configuration()
						.getString(MessageUtil.TABLE_NAME_USER_CONVERSATION),
				ConversationSummary.class);*/
		List<ConversationSummary> conversationSummaries = conversationSummaryRepo.findByUserId(userId);

		logger.debug(" Updating mail box for " + userId);
		List<ConversationSummary> conversationList;
		UserMailBoxInfo info = userMailBoxInfoRepo.findByUserId(userId);

		StringBuilder builder = new StringBuilder();
		builder.append(userId).append("_");
		String key = builder.toString();
		info.unreadConversationCount = 0;
		info.conversationCount = 0;
		do {

			conversationList = conversationSummaries;

			if (CollectionUtils.isNotEmpty(conversationList)) {
				for (ConversationSummary conversationSummary : conversationList) {
					logger.debug(" userId " + userId + " Current conversation summary "
							+ conversationSummary.getStatus());
					if (conversationSummary.getStatus() == ConversationStatus.UNREAD) {
						info.incrementUnread();
					}
					info.incrementConversationCount();
					//key = conversationSummary.getKey();
				}
			}

		} while (CollectionUtils.isNotEmpty(conversationList)
				&& conversationList.size() == MAXIMUM_BATCH_COUNT);

		userMailBoxInfoRepo.save(info);

		return true;
	}

	public Set<String> getIdsByTime(long minTimeCreated, long maxTimeCreated, int start, int size) {

		return getIdsByTime(minTimeCreated, maxTimeCreated, start, size, null);
	}

	public Set<String> getIdsByTime(long minTimeCreated, long maxTimeCreated, int start, int size,
									VedantuRecordState state) {

		List<User> models = userRepo.findByTimeCreated(minTimeCreated);
		Set<String> ids = new HashSet<String>();
		for (User model : models) {
			ids.add(model._getStringId());
		}
		return ids;
	}

	public GetConversationUsersRes getConversationUsers(GetConversationUsersReq request)
			throws VedantuException {

		GetConversationUsersRes response = new GetConversationUsersRes();
		ConversationRes conversation = getConversation(request.userId, request.conversationId);

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
			logger.debug(" NUmber of participants in this list " + conversation.getParticipants());
			List<SrcEntity> list = new ArrayList<SrcEntity>();
			list.addAll(conversation.getParticipants());
			list.removeAll(excludes);
			Collections.sort(list);
			request.start = request.start < 0 || request.start >= list.size() ? 0 : request.start;
			request.size = request.size < 0 ? 0 : (request.size > list.size() ? list.size()
					: request.size);

			List<SrcEntity> requestedUserList = list.subList(request.start, request.size);
			if (CollectionUtils.isNotEmpty(requestedUserList)) {
				logger.debug(" Populating users now ");
				response.list.addAll(messageUtil.populateUserNewsEntityDetails(request.orgId,
						requestedUserList));

				response.totalHits = conversation.getParticipants().size();
			}

			return response;
		}
		logger.debug(" No  of participants in this list ");
		return null;

	}

	public GetConversationRes getConversation(GetConversationReq request) throws VedantuException {

		ConversationRes conversation = getConversation(request.userId, request.conversationId);
		conversation.setParticipants(messageUtil.populateUserNewsEntityDetails(request.orgId,
				conversation.getParticipants()));
		GetConversationRes reservationRes = new GetConversationRes();
		reservationRes.conversation = conversation;

		return reservationRes;
	}

	public List<com.lms.models.messages.MessageSummary> getMessageSum(String orgId,
																	  List<Conversation> conversations) {

		List<com.lms.models.messages.MessageSummary> messageSummaries = new ArrayList<MessageSummary>();
		for (Conversation mongoSummary : conversations) {
			com.lms.models.messages.MessageSummary messageSummary = new com.lms.models.messages.MessageSummary(mongoSummary.recentMessageId);


			messageSummary.conversationId = mongoSummary.conversationId;
		/*	if(messageSummary!=null&&messageSummary.sender.getType()!=null&&messageSummary.sender.id!=null)
				messageSummary.sender = new SrcEntity(mongoSummary.u.type, mongoSummary.sender.id);
			messageSummary.types = mongoSummary.types;*/
			//messageSummary.numOfParticipants = mongoSummary.getTotalParticipants();
			messageSummary.orgId = mongoSummary.orgId;
			messageSummary.messageId = getUserMessageId(new SrcEntity(EntityType.USER, mongoSummary.userId), mongoSummary.firstMesssageId);
			messageSummary.conversationId = mongoSummary.conversationId;

			//	messageSummary.setStatus(mongoSummary.status);

			//messageSummary.setTimestamp(messageSummary.getTimestamp());

			//messageSummary.setMostRecentSender(messageUtil.populateUserNewsEntityDetails(orgId, messageSummary.getMostRecentSender()));
			annotateImages(messageSummary);

			messageSummaries.add(messageSummary);

		}
		return messageSummaries;
	}

	public MarkConversationRes markConversation(MarkConversationReq request) {
		boolean success = markConversation(request.userId, request.userConversationId,
				request.status);

		return new MarkConversationRes(success, request.userConversationId);
	}

	private boolean markConversation(@NotBlank(message = "User ID is required") String userId,
									 @NotBlank(message = "userConversationId should not be empty") String userConversationId,
									 @NotNull(message = "status should not be empty") ConversationStatus newStatus) {

		String[] userConversationSplitted = userConversationId.split("_");
		logger.debug("Marking conversation " + userConversationId + " status " + newStatus);

		ConversationSummary conversationSummary = find(userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX],
				userConversationSplitted[USER_CONVERSAION_USER_ID_INDEX]);

		logger.debug("Current  conversation " + userConversationId + " status " + conversationSummary.status);
		if (conversationSummary.status == newStatus) {
			logger.debug("No status update as  both requested and existing status is same" + userConversationId
					+ " status " + conversationSummary.status);
			return true;
		}

		conversationSummary = resetMessageUnread(conversationSummary._getStringId(), newStatus);

		if (newStatus == ConversationStatus.UNREAD) {
			incUnreadConversations(userId, 1);
		} else {
			decUnreadConversations(userId, 1);

		}
		return true;

	}

	public ConversationSummary resetMessageUnread(String conversationSummaryId, ConversationStatus status) {
		Optional<ConversationSummary> conversationSummaryOptional = conversationSummaryRepo
				.findById(conversationSummaryId);
		ConversationSummary conversationSummary = null;
		if (conversationSummaryOptional.isPresent()) {
			conversationSummary = conversationSummaryOptional.get();
		} else {
			return null;
		}

		if (status == ConversationStatus.READ) {
			conversationSummary.setMessagesUnread(Integer.valueOf(0));

		}
		conversationSummary.setStatus(status);

		ConversationSummary updateResults = conversationSummaryRepo.save(conversationSummary);

		if (updateResults == null) {
			logger.debug(" failed to update messageUnread and messageCount");
			return null;
		}
		return updateResults;

	}

	public boolean incUnreadConversations(String userId, int count) {

		return updateUnreadConversations(userId, count, NumberUpdate.INCREMENT);
	}

	private boolean updateUnreadConversations(String userId, int count, NumberUpdate update) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		Update updateOperations = new Update();
		criteria.and("userId").is(userId);
		if (update == NumberUpdate.DECREMENT) {
			criteria.and("unreadConversationCount").gte(count);
			updateOperations.inc("unreadConversationCount", Integer.valueOf((0 - count)));
		} else {
			updateOperations.inc("unreadConversationCount", Integer.valueOf(count));
		}
		query.addCriteria(criteria);
		UpdateResult updateResults = mongoTemplate.updateMulti(query, updateOperations, UserMailBoxInfo.class);

		if (updateResults != null) {
			logger.debug(" Updated messageUnread and messageCount");
			return true;
		}
		return false;

	}

	public boolean decUnreadConversations(String userId, int count) {

		return updateUnreadConversations(userId, count, NumberUpdate.DECREMENT);
	}

	public DeleteConversationRes deleteUserConversation(DeleteConversationReq request) {
		boolean isDeleted = deleteUserConversation(request.userId, request.userConversationId);
		DeleteConversationRes response = new DeleteConversationRes();
		response.deleted = isDeleted;
		return response;
	}

	private boolean deleteUserConversation(@NotBlank(message = "User ID is required") String userId,
										   @NotBlank(message = "userConversationId should not be empty") String userConversationId) {
		if (userConversationId.startsWith(userId)) {
			String[] userConversationSplitted = userConversationId.split("_");
			// delete all other messages
			Conversation conversation = getConversationById(userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX]);

			ConversationSummary olderSummary = markDeleted(
					userConversationSplitted[USER_CONVERSAION_CONVERSATION_ID_INDEX],
					userConversationSplitted[USER_CONVERSAION_USER_ID_INDEX]);
			deleteConversation(userId,
					olderSummary.status == ConversationStatus.UNREAD,
					conversation.userId.equals(olderSummary.userId));

			return true;
		}
		return false;
	}

	private Conversation getConversationById(String id) {

		return conversationRepo.findById(id).get();
	}

	public ConversationSummary markDeleted(String summaryId, String userId) {
		ConversationSummary conversationSummary = conversationSummaryRepo.findByConversationIdAndUserIdAndRecordState(summaryId, userId, VedantuRecordState.ACTIVE);
		conversationSummary.setRecordState(VedantuRecordState.DELETED);
		ConversationSummary olderSummary = conversationSummaryRepo.save(conversationSummary);

		if (olderSummary != null) {
			logger.debug(" Updated messageUnread and messageCount");
			return olderSummary;
		}
		return null;
	}

	public boolean deleteConversation(String userId, boolean deletingUnreadConversation,
									  boolean sender) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		Update updateOperations = new Update();

		criteria.and("userId").is(userId);

		if (deletingUnreadConversation) {
			criteria.and("unreadConversationCount").gte(1);
			updateOperations.inc("unreadConversationCount", Integer.valueOf(-1));
		}

		if (sender) {
			criteria.and("sentCount").gte(1);
			updateOperations.inc("sentCount", Integer.valueOf(-1));
		}
		criteria.and("conversationCount").gte(1);
		updateOperations.inc("conversationCount", Integer.valueOf(-1));

		UpdateResult updateResults = mongoTemplate.updateMulti(query, updateOperations, UserMailBoxInfo.class);

		if (updateResults != null) {
			logger.debug(" Updated messageUnread and messageCount");
			return true;
		}
		return false;

	}


}
