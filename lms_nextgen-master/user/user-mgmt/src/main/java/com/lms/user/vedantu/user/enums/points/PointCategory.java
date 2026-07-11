package com.lms.user.vedantu.user.enums.points;

import com.lms.common.vedantu.event.api.IEventDetails;

public enum PointCategory {
	LOGIN(0, 0), SIGN_UP(0, 100), UPLOAD(80, 20), CREATE_PLAYLIST(50, 20), ADD_TO_UPLOADED(
			20, 0), FOLLOW(0, 0), VIEW(0, 0), VOTE(10, 0), RATE(0, 0) {
		@Override
		public int getToOwner(IEventDetails iEventDetails) {
			// RateEntityDetails details = (RateEntityDetails) iEventDetails;
			int totalPoins = 0;
			// if (details.rating == 5) {
			// totalPoins = 50;
			// } else if (details.rating == 4) {
			// totalPoins = 30;
			// } else if (details.rating == 3) {
			// totalPoins = 10;
			// }
			return totalPoins;
		}
	},
	JOIN_GROUP(0, 0), ADD_TOC(30, 10), STAR(0, 0), ADD_QUESTION(10, 0), ADD_SOLUTION(
			10, 0), CREATE_TEST(10, 0), ATTEMPT_QUESTION(10, 10) {
		@Override
		public int getToActor(IEventDetails iEventDetails) {

			// QuestionAnalyticsUpdateDetails details =
			// (QuestionAnalyticsUpdateDetails) iEventDetails;
			int totalPoints = 10;
			// if (details.isCorrect && details.legal) {
			// totalPoints += 50;
			// }
			return totalPoints;
		}
	},
	ATTEMPT_TEST(10, 10) {
		@Override
		public int getToActor(IEventDetails iEventDetails) {
			// TestAnalyticsUpdateDetails details = (TestAnalyticsUpdateDetails)
			// iEventDetails;
			// double percentage = details.totalMarks != 0 ?
			// ((details.scoredMarks * 100) / details.totalMarks)
			// : 0;
			// long total = details.corrects + details.inCorrects;
			// total = Math.max(total, 1);
			// double accuracy = (details.corrects * 100) / total;
			// return PointUtil.getTestAccuracyPoint(accuracy)
			// + PointUtil.getTestScorePoint(percentage);
			return 0;
		}
	},
	CHALLENGE(0, 0),

	COMMENT(50, 40),

	NO_POINTS(0, 0);

	private int toOwner;
	private int toActor;

	private PointCategory(int toOwner, int toActor) {

		this.toOwner = toOwner;
		this.toActor = toActor;
	}

	public int getToOwner(IEventDetails iEventDetails) {
		return toOwner;
	}

	public int getToActor(IEventDetails details) {
		return toActor;
	}
}
