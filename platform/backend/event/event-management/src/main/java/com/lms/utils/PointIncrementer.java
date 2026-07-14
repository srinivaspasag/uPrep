package com.lms.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.mongo.Event;
import com.lms.user.vedantu.user.enums.points.PointCategory;
import com.lms.user.vedantu.user.model.points.PointInfo;
import com.lms.user.vedantu.user.model.points.UserPoints;
import com.lms.user.vedantu.user.model.points.UserPointsDetail;
import com.lms.user.vedantu.user.repository.points.UserPointsDetailRepo;
import com.lms.user.vedantu.user.repository.points.UserPointsRepo;
@Component
public class PointIncrementer {

	private static final Logger logger = LoggerFactory.getLogger(PointIncrementer.class);

	private static PointIncrementer instance;
	private Map<EventType, PointCategory> eventPointsMap;
	@Autowired
	private UserPointsRepo userPointsRepo;
	@Autowired
    private UserPointsDetailRepo userPointsDetailRepo;
	private PointIncrementer() {
		this.eventPointsMap = new HashMap<EventType, PointCategory>();
		// load map for event to point category type..

		eventPointsMap.put(EventType.INDEX_QUESTION, PointCategory.ADD_QUESTION);
		eventPointsMap.put(EventType.ADD_SOLUTION, PointCategory.ADD_SOLUTION);
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
		String ownerId = "";//EntityTypeDAOFactory.INSTANCE.getOwnerId(srcEntity);

		logger.debug("source owner id is : " + ownerId + " for srcEntiry : " + srcEntity + " and actorId : " + actorId);
		PointCategory pointCategory = getPointCategory(event.getType());
		if (null == pointCategory) {
			logger.error("null pointCategory for eventType : " + event.getType());
			return;
		}
		if (!StringUtils.isEmpty(actorId)) {
			logger.info("increasing points for userId : " + actorId + " as actor, pointCategory : " + pointCategory);
			if (pointCategory.getToActor(event.fetchEventDetails()) != 0) {
				synchronized (actorId.intern()) {
					incrementToActor(actorId, event);
				}
			} else {
				logger.info("no points are available for " + event.getType() + " to actor : "
						+ pointCategory.getToActor(event.fetchEventDetails()));
			}
		}

		if (!StringUtils.isEmpty(ownerId) && !ownerId.equals(actorId)) {
			logger.info("increasing points for userId : " + ownerId + " as owner");
			if (pointCategory.getToOwner(event.fetchEventDetails()) != 0) {
				synchronized (ownerId.intern()) {
					incrementToOwner(ownerId, event);
				}
			}
		}

			}

	private void incrementToActor(String actorId, Event event) {
		EventType eType = event.getType();
		logger.info("incrementing points as actor for userId : " + actorId + ", eType : " + eType);
		UserPoints userPoint = getUserPoints(actorId);

		Set<PointInfo> pointInfos = userPoint.pointInfos;
		PointCategory pointCategory = getPointCategory(eType);

		if (pointInfos == null) {
			pointInfos = new HashSet<PointInfo>();
			userPoint.pointInfos = pointInfos;
		}
		PointInfo pointInfo = new PointInfo(pointCategory);
		saveUserPointDetails(actorId, event, pointCategory.getToActor(event.fetchEventDetails()));
		if (!pointInfos.contains(pointInfo)) {
			pointInfo.setPointAsActor(pointCategory.getToActor(event.fetchEventDetails()));
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
			logger.info("previous points are : " + pointAsActor + " totalEventAsActor" + totalEventAsActor
					+ " for userId : " + actorId);
			pointAsActor = pointAsActor + pointCategory.getToActor(event.fetchEventDetails());
			totalEventAsActor++;
			pointInfo.setPointAsActor(pointAsActor);
			pointInfo.setTotalEventAsActor(totalEventAsActor);
			logger.info("new points are : " + pointAsActor + " totalEventAsActor" + totalEventAsActor + "for userId :"
					+ actorId);
			pointInfos.add(pointInfo);
		}

		saveUserPoints(userPoint, event);
	}

	private void incrementToOwner(String ownerId, Event event) {
		EventType eType = event.getType();
		UserPoints userPoint = getUserPoints(ownerId);

		Set<PointInfo> pointInfos = userPoint.pointInfos;
		PointCategory pointCategory = getPointCategory(eType);

		if (pointInfos == null) {
			logger.info("pointInfos is empty");
			pointInfos = new HashSet<PointInfo>();
			userPoint.pointInfos = pointInfos;
		}
		PointInfo pointInfo = new PointInfo(pointCategory);
		saveUserPointDetails(ownerId, event, pointCategory.getToOwner(event.fetchEventDetails()));
		if (!pointInfos.contains(pointInfo)) {
			pointInfo.setPointAsOwner(pointCategory.getToOwner(event.fetchEventDetails()));
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
			logger.info("previous points are : " + pointAsOwner + " and totalEventAsOwner : " + totalEventAsOwner
					+ " for userId : " + ownerId);
			pointAsOwner = pointAsOwner + pointCategory.getToOwner(event.fetchEventDetails());
			totalEventAsOwner++;
			pointInfo.setPointAsOwner(pointAsOwner);
			pointInfo.setTotalEventAsOwner(totalEventAsOwner);
			logger.info("new points are : " + pointAsOwner + " and totalEventAsOwner : " + totalEventAsOwner
					+ "for userId :" + ownerId);
			pointInfos.add(pointInfo);
		}
		saveUserPoints(userPoint, event);
	}

	private void saveUserPointDetails(String userId, Event event, int points) {

		logger.info("saving userPointDetails for userId : " + userId);
		PointCategory pointCategory = getPointCategory(event.getType());
		addUserPointsDetail(userId, event.srcEntity, pointCategory, points);
	}

	public void saveUserPoints(UserPoints userPoints, Event event) {
		logger.info("saving userPoint " + userPoints);
		if (userPoints != null) {
			logger.info("allotting title to userId : " + userPoints.userId);
			userPoints.accumulateTotalPoints();
			userPointsRepo.save(userPoints);

			logger.info("updating user[" + userPoints.userId + "] points in es");

		}
	}

	private PointCategory getPointCategory(EventType eType) {

		return eventPointsMap.get(eType);
	}
	public UserPoints getUserPoints(String userId) {
		UserPoints userPoints = userPointsRepo.findByUserId(userId);
		if (userPoints == null) {
			userPoints = new UserPoints(userId);
			userPointsRepo.save(userPoints);
		}
		return userPoints;
	}
	public UserPointsDetail addUserPointsDetail(String userId,
			SrcEntity srcEntity, PointCategory pointCategory, int points) {
		UserPointsDetail pointsDetails = new UserPointsDetail(userId,
				srcEntity, pointCategory, points);
		userPointsDetailRepo.save(pointsDetails);
		return pointsDetails;
	}

}
