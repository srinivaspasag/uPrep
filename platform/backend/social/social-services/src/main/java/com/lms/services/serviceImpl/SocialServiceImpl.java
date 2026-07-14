package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EventUtil;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.EntityUserActionManager;
import com.lms.managers.AbstractContentManager;
import com.lms.models.OrgMember;
import com.lms.pojo.SendEmailToStudentsDetails;
import com.lms.requests.AddEntityUserActionReq;
import com.lms.requests.GetEntityUserActionUsersReq;
import com.lms.requests.RemoveEntityUserActionReq;
import com.lms.requests.SendEmailReq;
import com.lms.response.EntityUserActionRes;
import com.lms.response.EntityUserActionUsersRes;
import com.lms.response.SendEmailRes;
import com.lms.services.SocialService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.utils.EntityUserActionUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SocialServiceImpl extends AbstractContentManager implements SocialService {
	private static final Logger logger = LoggerFactory.getLogger(SocialServiceImpl.class);

	@Autowired
	private EntityUserActionManager entityUserActionManager;
	@Autowired
	private EntityUserActionUtils entityUserActionUtils;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private EventUtil eventUtil;

	@Override
	public VedantuResponse view(AddEntityUserActionReq viewReq) {
		EntityUserActionRes viewRes = null;
		try {
			viewRes = entityUserActionManager.addEntityUserAction(viewReq, UserActionType.VIEWED, true);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(viewRes);
	}

	@Override
	public VedantuResponse unfollow(RemoveEntityUserActionReq removeEntityUserActionReq) {
		EntityUserActionRes unFollowRes = null;

		unFollowRes = entityUserActionManager.removeEntityUserAction(
				removeEntityUserActionReq, UserActionType.FOLLOWING);

		return new VedantuResponse(unFollowRes);
	}

	@Override
	public VedantuResponse getfollowers(GetEntityUserActionUsersReq getEntityUserActionUsersReq) {
		if (getEntityUserActionUsersReq.getEntity() == null || getEntityUserActionUsersReq.getEntity().getType() == null || getEntityUserActionUsersReq.getEntity().getId() == null) {
			throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "entityid or entityType should not be null");
		}
		EntityUserActionUsersRes getFollowRes = entityUserActionManager.getEntityUserActionUsers(
				getEntityUserActionUsersReq, UserActionType.FOLLOWING);
		return new VedantuResponse(getFollowRes);
	}

	public void generateEventAysc(final String userId, final IEventDetails details,
								  final EventType eventType) {

		generateEventAysc(userId, details, eventType, 0);
	}

	public void generateEventAysc(final String userId, final IEventDetails details,
								  final EventType eventType, final long processTime) {
		CompletableFuture.runAsync(() -> {
			eventUtil.generateEvent(eventType, null, userId, details,
					details.__getSrcEntity(), UserActionType.EventActionType.ADD, processTime);
		});

	}

	@Override
	public VedantuResponse upVote(AddEntityUserActionReq upVoteReq) {
		EntityUserActionRes upVoteRes = null;
		try {
			upVoteRes = entityUserActionManager.addEntityUserAction(upVoteReq, UserActionType.VOTED);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(upVoteRes);
	}

	@Override
	public VedantuResponse follow(AddEntityUserActionReq followReq) {
		EntityUserActionRes followRes = null;
		try {
			followRes = entityUserActionManager.addEntityUserAction(followReq, UserActionType.FOLLOWING);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(followRes);
	}

	@Override
	public VedantuResponse getViewers(GetEntityUserActionUsersReq getReq, UserActionType actionType) {
		AtomicLong totalHits = new AtomicLong();
		List<String> userIds = entityUserActionManager.getEntityUserActionByIds(getReq.entity,
				actionType, getReq.start, getReq.size, getReq.orderBy, getReq.sortOrder, totalHits);
		Map<String, ModelBasicInfo> usersMap = getUserInfoMap(null, userIds);
		EntityUserActionUsersRes res = new EntityUserActionUsersRes();
		res.totalHits = totalHits.longValue();
		UserInfo userInfo = new UserInfo("", VedantuRecordState.ACTIVE);
		for (String userId : userIds) {
			userInfo.setId(userId);
			res.list.add(userInfo);
		}
		return new VedantuResponse(res);
	}

	public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
													  Collection<String> userIds) {

		return getUserInfoMap(orgId, userIds, false);
	}

	public Map<String, ModelBasicInfo> getUserInfoMap(String orgId, Collection<String> userIds,
													  boolean excludeOrgMappingInfo) {

		logger.info("getUserInfoMap orgId:" + orgId + ", userIds: " + userIds);
		if (CollectionUtils.isEmpty(userIds)) {
			return new HashMap<String, ModelBasicInfo>();
		}
		Query query = new Query();
		Criteria criteria = new Criteria();
		DBObject memberQuery = new BasicDBObject();

		boolean isOrgReq = !StringUtils.isEmpty(orgId);

		if (isOrgReq) {
			criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
			criteria.and(ConstantsGlobal.USER_ID).in(userIds);

		} else {
			criteria.and(ConstantsGlobal._ID).in(userIds);
		}
		query.addCriteria(criteria);
		List<OrgMember> orgMemberList = mongoTemplate.find(query, OrgMember.class);
		List<User> users = mongoTemplate.find(query, User.class);

		Map<String, ModelBasicInfo> userIdToBasicInfoMap = isOrgReq
				? null
				: toBasicUserInfoMap(users);

		logger.debug("userIds map : " + userIdToBasicInfoMap);
		return userIdToBasicInfoMap;

	}

	public Map<String, ModelBasicInfo> toBasicUserInfoMap(List<User> results) {
		ModelBasicInfo modelBasicInfo = new ModelBasicInfo();
		Map<String, ModelBasicInfo> infosMap = new LinkedHashMap<String, ModelBasicInfo>();
		if (!CollectionUtils.isEmpty(results)) {
			for (User user : results) {
				if (null == user) {
					continue;
				}
				modelBasicInfo.setId(String.valueOf(user.getId()));
				modelBasicInfo.setRecordState(user.getRecordState());

				infosMap.put(user._getStringId(), modelBasicInfo);

			}
		}
		return infosMap;
	}

	@Override
	public VedantuResponse completed(AddEntityUserActionReq addReq, UserActionType actionType, boolean allowDuplicates) {


		isSocialActionAllowed(addReq.entity.type, addReq.entity.id);
		EntityUserActionRes addRes = new EntityUserActionRes();

		addRes.processed = entityUserActionUtils.addEntityUserAction(addReq.userId, addReq.entity,
				addReq.context, actionType, allowDuplicates);
		return new VedantuResponse(addRes);
	}

	@Override
	public VedantuResponse getvoters(GetEntityUserActionUsersReq getEntityUserActionUsersReq) {
		EntityUserActionUsersRes getVotersRes = entityUserActionManager.getEntityUserActionUsers(
				getEntityUserActionUsersReq, UserActionType.VOTED);
		return new VedantuResponse(getVotersRes);
	}

	@Override
	public VedantuResponse sendemail(SendEmailReq sendEmailReq) {
		SendEmailRes response = sendEmail(sendEmailReq);
		return new VedantuResponse(response);
	}

	public SendEmailRes sendEmail(SendEmailReq request) {
		SendEmailRes response = new SendEmailRes();
		SendEmailToStudentsDetails details = null;
		String newLine = System.getProperty("line.separator");
		try {
			details = new SendEmailToStudentsDetails();
		} catch (ClassNotFoundException e) {
			//lo.error("SendEmailToStudentsDetails class not found", e);
		}
		if (request.fromForm.equals("PROGRAMS")) {
			details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
			details.setSubject("Student has a query!");
			details.message = "Mobile number of prospective client : " + request.number;
		} else if (request.fromForm.equals("ENQUIRY")) {
			details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
			details.setSubject("Franchise Enquiry!");
			details.message = "Name :" + request.name + newLine + "Email :" + request.email
					+ newLine + "Number :" + request.number + newLine + "City :" + request.city + newLine + "Investment :" + request.investment + newLine
					+ "Questions if any :" + request.que_message;
		} else if (request.fromForm.equals("INSTITUTES")) {
			details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
			details.setSubject("B2B Enquiry!");
			details.message = "Name :" + request.name + newLine + "Email :" + request.email
					+ newLine + "Message :" + request.message + newLine + "Number :"
					+ request.number;
		} else if (request.fromForm.equals("PRICING")) {
			details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
			details.setSubject("Enterprise Pack Plan Enquiry!");
			details.message = "Name :" + request.name + newLine + "Email :" + request.email
					+ newLine + "Message :" + request.message + newLine + "Number :"
					+ request.number;
		} else {
			details.addBccRecepient("Admin Learnpedia", "admin@learnpedia.in");
			details.setSubject("Get in touch!");
			details.message = "Name :" + request.name + newLine + "Email :" + request.email
					+ newLine + "Message :" + request.message + newLine + "Number :"
					+ request.number;
		}
		//generateEventAysc("", details, EventType.SEND_EMAIL);
		response.success = true;
		return response;
	}

}
