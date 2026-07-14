package com.lms.user.vedantu.user.model.points;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.user.vedantu.user.enums.points.PointCategory;
import com.lms.user.vedantu.user.enums.points.Titles;

@Document(value = "userpoints")
public class UserPoints extends VedantuBaseMongoModel {
	private static final Logger logger = LoggerFactory.getLogger(UserPoints.class);
	@Indexed
	public String userId;
	public Set<PointInfo> pointInfos;
	public long totalPoints;
	public Titles title;

	public UserPoints() {
		super();
	}

	public UserPoints(String userId) {
		super();
		this.userId = userId;
		this.pointInfos = new HashSet<PointInfo>();
		this.title = Titles.NEWBIE;
	}

	public void addPointInfoAsActor(PointCategory pointCategory, int point) {
		PointInfo pointInfo = new PointInfo(pointCategory);

		if (!pointInfos.contains(pointInfo)) {
			pointInfo.setPointAsActor(point);
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
			logger.info("previous points are : " + pointAsActor + " and totalEventAsActor : " + totalEventAsActor
					+ " for userId : " + userId);
			pointAsActor = pointAsActor + point;
			totalEventAsActor++;
			pointInfo.setPointAsActor(pointAsActor);
			pointInfo.setTotalEventAsActor(totalEventAsActor);
			logger.info("new points are : " + pointAsActor + " and totalEventAsActor : " + totalEventAsActor
					+ "for userId :" + userId);
		}
		accumulateTotalPoints();
	}

	public void accumulateTotalPoints() {
		logger.info("previous usr total points: " + this.totalPoints);
		long totalPoints = 0;
		if (pointInfos != null) {
			for (PointInfo pInfo : pointInfos) {
				totalPoints += pInfo.cumulateActions();
			}
		}
		this.totalPoints = totalPoints;
		logger.info("new total points: " + this.totalPoints);
	}
}
