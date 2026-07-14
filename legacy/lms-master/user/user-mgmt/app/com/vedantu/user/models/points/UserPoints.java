package com.vedantu.user.models.points;

import java.util.HashSet;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.enums.points.PointCategory;
import com.vedantu.enums.points.Titles;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userpoints")
public class UserPoints extends VedantuBaseMongoModel {
	private static final ALogger LOGGER = Logger.of(UserPoints.class);
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
			LOGGER.info("previous points are : " + pointAsActor
					+ " and totalEventAsActor : " + totalEventAsActor
					+ " for userId : " + userId);
			pointAsActor = pointAsActor + point;
			totalEventAsActor++;
			pointInfo.setPointAsActor(pointAsActor);
			pointInfo.setTotalEventAsActor(totalEventAsActor);
			LOGGER.info("new points are : " + pointAsActor
					+ " and totalEventAsActor : " + totalEventAsActor
					+ "for userId :" + userId);
		}
		accumulateTotalPoints();
	}

	public void accumulateTotalPoints() {
		LOGGER.info("previous usr total points: " + this.totalPoints);
		long totalPoints = 0;
		if (pointInfos != null) {
			for (PointInfo pInfo : pointInfos) {
				totalPoints += pInfo.cumulateActions();
			}
		}
		this.totalPoints = totalPoints;
		LOGGER.info("new total points: " + this.totalPoints);
	}
}
