package com.vedantu.eventbus.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.enums.points.PointCategory;
import com.vedantu.events.models.Event;
import com.vedantu.user.daos.UserPointsDAO;
import com.vedantu.user.daos.UserPointsDetailDAO;
import com.vedantu.user.models.points.PointInfo;
import com.vedantu.user.models.points.UserPoints;

public class PointIncrementer {

	private static final ALogger LOGGER = Logger.of(PointIncrementer.class);

	private static PointIncrementer instance;
	private Map<EventType, PointCategory> eventPointsMap;

	private PointIncrementer() {
		this.eventPointsMap = new HashMap<EventType, PointCategory>();
		// load map for event to point category type..

		// eventPointsMap.put(EventType.COMMENT, PointCategory.COMMENT);
		// eventPointsMap.put(EventType.FOLLOW_DOC, PointCategory.FOLLOW_DOC);
		// eventPointsMap.put(EventType.FOLLOW_USER, PointCategory.FOLLOW_USER);
		// eventPointsMap.put(EventType.LOGOUT, PointCategory.LOGIN);
		// eventPointsMap.put(EventType.RATE, PointCategory.RATE);

		// eventPointsMap.put(EventType.UNFOLLOW_DOC, PointCategory.NO_POINTS);
		// eventPointsMap.put(EventType.UNFOLLOW_USER, PointCategory.NO_POINTS);

		// eventPointsMap.put(EventType.STAR_DOC, PointCategory.STAR_DOC);

		// eventPointsMap.put(EventType.ADD_TOC, PointCategory.ADD_TOC);
		// eventPointsMap.put(EventType.UPLOAD_DOC, PointCategory.UPLOAD);
		// eventPointsMap.put(EventType.JOIN_GROUP, PointCategory.JOIN_GROUP);
		// eventPointsMap.put(EventType.LOGIN, PointCategory.LOGIN);
		// eventPointsMap.put(EventType.SIGNUP, PointCategory.SIGN_UP);

		eventPointsMap
				.put(EventType.INDEX_QUESTION, PointCategory.ADD_QUESTION);
		eventPointsMap.put(EventType.ADD_SOLUTION, PointCategory.ADD_SOLUTION);
		// eventPointsMap.put(EventType.INDEX_PLAYLIST,
		// PointCategory.CREATE_PLAYLIST);
		// eventPointsMap.put(EventType.INDEX_TEST, PointCategory.CREATE_TEST);

		// eventPointsMap.put(EventType.FOLLOW_ENTITY, PointCategory.FOLLOW);
		// eventPointsMap.put(EventType.ATTEMPT_ENTITY,
		// PointCategory.ATTEMPT_QUESTION);
		// eventPointsMap.put(EventType.QUESTION_OVERALL_ANALYTICS,
		// PointCategory.ATTEMPT_QUESTION);
		// eventPointsMap.put(EventType.TEST_OVERALL_ANALYTICS,
		// PointCategory.ATTEMPT_TEST);
		// eventPointsMap.put(EventType.RATE_ENTITY, PointCategory.RATE);
		// eventPointsMap.put(EventType.VIEW_ENTITY, PointCategory.VIEW);
		// eventPointsMap.put(EventType.STAR_ENTITY, PointCategory.STAR);
		// eventPointsMap.put(EventType.VOTE_ENTITY, PointCategory.VOTE);
		eventPointsMap.put(EventType.END_CHALLENGE, PointCategory.CHALLENGE);
	}

	public static PointIncrementer getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new PointIncrementer();
		}
	}

	public void incrementPoint(Event event) {

		String actorId = event.getUserId();
		SrcEntity srcEntity = event.srcEntity;
		if (srcEntity == null) {
			return;
		}
		String ownerId = EntityTypeDAOFactory.INSTANCE.getOwnerId(srcEntity);

		LOGGER.debug("source owner id is : " + ownerId + " for srcEntiry : "
				+ srcEntity + " and actorId : " + actorId);
		PointCategory pointCategory = getPointCategory(event.getType());
		if (null == pointCategory) {
			LOGGER.error("null pointCategory for eventType : "
					+ event.getType());
			return;
		}
		if (StringUtils.isNotEmpty(actorId)) {
			LOGGER.info("increasing points for userId : " + actorId
					+ " as actor, pointCategory : " + pointCategory);
			if (pointCategory.getToActor(event.fetchEventDetails()) != 0) {
				synchronized (actorId.intern()) {
					incrementToActor(actorId, event);
				}
			} else {
				LOGGER.info("no points are available for " + event.getType()
						+ " to actor : "
						+ pointCategory.getToActor(event.fetchEventDetails()));
			}
		}

		if (StringUtils.isNotEmpty(ownerId)
				&& !StringUtils.equals(ownerId, actorId)) {
			LOGGER.info("increasing points for userId : " + ownerId
					+ " as owner");
			if (pointCategory.getToOwner(event.fetchEventDetails()) != 0) {
				synchronized (ownerId.intern()) {
					incrementToOwner(ownerId, event);
				}
			}
		}

		/*
		 * this code was used for increasing poin of baseEntity owner in case
		 * when some hase replied someone comment on owner document EntityType
		 * baseType = null; String baseId = null; if (event.getType() ==
		 * EventType.VOTE_UP) { VoteUpDetails details = (VoteUpDetails)
		 * event.fetchEventDetails(); baseId = details.baseId; baseType =
		 * details.baseType; } else if (event.getType() == EventType.RATE ||
		 * event.getType() == EventType.STAR_DOC) {
		 * AbstractUserDocInteractionDetails details =
		 * (AbstractUserDocInteractionDetails) event .fetchEventDetails();
		 * baseId = details.docId; baseType = EntityType.DOCUMENT; } else if
		 * (event.getType() == EventType.COMMENT || srcEntity.type ==
		 * EntityType.HIGHLIGHT) { CommentSearchIndexDetails details =
		 * (CommentSearchIndexDetails) event .fetchEventDetails(); baseId =
		 * details.getBaseId(); baseType = details.getBaseType(); } String
		 * docOwnerId = baseType.getOwnerId(baseId); if
		 * (StringUtils.equals(actorId, docOwnerId)) {
		 * LOGGER.info("actor and srcOwner are same " + docOwnerId +
		 * ", hence returning from here "); return; }
		 * LOGGER.info("increasing points for eType : [" + event.getType() +
		 * " ] as an uploader for userId : " + docOwnerId); LOGGER .info(
		 * "srcEntity type is a comment, hence incrementing points for docOwnerId : "
		 * + docOwnerId); if (StringUtils.isNotEmpty(docOwnerId) &&
		 * !StringUtils.equals(docOwnerId, actorId)) {
		 * LOGGER.info("increasing points for userId (docOwnerId) : " +
		 * docOwnerId + " as uploader/doc owner"); if
		 * (pointCategory.getToOwner() != 0) { synchronized
		 * (docOwnerId.intern()) { incrementAsOwner(docOwnerId, event); } } }
		 */
	}

	private void incrementToActor(String actorId, Event event) {
		EventType eType = event.getType();
		LOGGER.info("incrementing points as actor for userId : " + actorId
				+ ", eType : " + eType);
		UserPoints userPoint = UserPointsDAO.INSTANCE.getUserPoints(actorId);

		Set<PointInfo> pointInfos = userPoint.pointInfos;
		PointCategory pointCategory = getPointCategory(eType);

		if (pointInfos == null) {
			pointInfos = new HashSet<PointInfo>();
			userPoint.pointInfos = pointInfos;
		}
		PointInfo pointInfo = new PointInfo(pointCategory);
		saveUserPointDetails(actorId, event,
				pointCategory.getToActor(event.fetchEventDetails()));
		if (!pointInfos.contains(pointInfo)) {
			pointInfo.setPointAsActor(pointCategory.getToActor(event
					.fetchEventDetails()));
			pointInfo.setTotalEventAsActor(1);
			pointInfos.add(pointInfo);
		} else {
			for (PointInfo pInfo : pointInfos) {
				if (pInfo.equals(pointInfo)) {
					pointInfo = pInfo;
					break;
				}
			}

			long pointAsActor = pointInfo.getPointAsActor();
			int totalEventAsActor = pointInfo.getTotalEventAsActor();
			LOGGER.info("previous points are : " + pointAsActor
					+ " totalEventAsActor" + totalEventAsActor
					+ " for userId : " + actorId);
			pointAsActor = pointAsActor
					+ pointCategory.getToActor(event.fetchEventDetails());
			totalEventAsActor++;
			pointInfo.setPointAsActor(pointAsActor);
			pointInfo.setTotalEventAsActor(totalEventAsActor);
			LOGGER.info("new points are : " + pointAsActor
					+ " totalEventAsActor" + totalEventAsActor + "for userId :"
					+ actorId);
			pointInfos.add(pointInfo);
		}

		saveUserPoints(userPoint, event);
	}

	private void incrementToOwner(String ownerId, Event event) {
		EventType eType = event.getType();
		UserPoints userPoint = UserPointsDAO.INSTANCE.getUserPoints(ownerId);

		Set<PointInfo> pointInfos = userPoint.pointInfos;
		PointCategory pointCategory = getPointCategory(eType);

		if (pointInfos == null) {
			LOGGER.info("pointInfos is empty");
			pointInfos = new HashSet<PointInfo>();
			userPoint.pointInfos = pointInfos;
		}
		PointInfo pointInfo = new PointInfo(pointCategory);
		saveUserPointDetails(ownerId, event,
				pointCategory.getToOwner(event.fetchEventDetails()));
		if (!pointInfos.contains(pointInfo)) {
			pointInfo.setPointAsOwner(pointCategory.getToOwner(event
					.fetchEventDetails()));
			pointInfo.setTotalEventAsOwner(1);
			pointInfos.add(pointInfo);
		} else {
			for (PointInfo pInfo : pointInfos) {
				if (pInfo.equals(pointInfo)) {
					pointInfo = pInfo;
					break;
				}
			}
			long pointAsOwner = pointInfo.getPointAsOwner();
			int totalEventAsOwner = pointInfo.getTotalEventAsOwner();
			LOGGER.info("previous points are : " + pointAsOwner
					+ " and totalEventAsOwner : " + totalEventAsOwner
					+ " for userId : " + ownerId);
			pointAsOwner = pointAsOwner
					+ pointCategory.getToOwner(event.fetchEventDetails());
			totalEventAsOwner++;
			pointInfo.setPointAsOwner(pointAsOwner);
			pointInfo.setTotalEventAsOwner(totalEventAsOwner);
			LOGGER.info("new points are : " + pointAsOwner
					+ " and totalEventAsOwner : " + totalEventAsOwner
					+ "for userId :" + ownerId);
			pointInfos.add(pointInfo);
		}
		saveUserPoints(userPoint, event);
	}

	private void saveUserPointDetails(String userId, Event event, int points) {

		LOGGER.info("saving userPointDetails for userId : " + userId);
		PointCategory pointCategory = getPointCategory(event.getType());
		UserPointsDetailDAO.INSTANCE.addUserPointsDetail(userId,
				event.srcEntity, pointCategory, points);
	}

	public void saveUserPoints(UserPoints userPoints, Event event) {
		LOGGER.info("saving userPoint " + userPoints);
		if (userPoints != null) {
			LOGGER.info("allotting title to userId : " + userPoints.userId);
			// TitleAllotter titleAllotter = new TitleAllotter();
			// titleAllotter.allot(userPoints);
			// LOGGER.info("allotting Badges to userId : "
			// + userPoints.getUserId());
			// BadgeAllotter badgeAllotter = BadgeAllotter.getInstance();
			// badgeAllotter.allot(userPoints, event);
			userPoints.accumulateTotalPoints();
			UserPointsDAO.INSTANCE.save(userPoints);

			LOGGER.info("updating user[" + userPoints.userId + "] points in es");

			// TODO: update user points in es for searching..
			// updating user total points in es
			// UserSearchIndexDetails userDetails = UserUtilCommon
			// .getUserFromES(userPoints.getUserId());
			// if (userDetails == null) {
			// Profile profile = UserUtilCommon.getProfile(userPoints
			// .getUserId());
			// userDetails = new UserSearchIndexDetails();
			// userDetails.fromMongoModel(profile);
			// }
			// userDetails.points = userPoints.totalPoints;
			// userDetails.setAction(SearchIndexUtil.UPDATE_INDEX);
			// UserSearchIndexProcessor userSearchIndexProcessor = new
			// UserSearchIndexProcessor();
			// userSearchIndexProcessor.process(event, userDetails, false);
		}
	}

	private PointCategory getPointCategory(EventType eType) {

		return eventPointsMap.get(eType);
	}

}
