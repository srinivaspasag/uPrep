package com.vedantu.eventbus.processors.challenges;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeLeaderBoardDAO;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.content.models.challenges.ChallengeLeaderBoard;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager.SortOrder;

public class ChallengeRankCalculatorProcessor extends
		AbstractSearchIndexProcessor {
	private static final ALogger LOGGER = Logger
			.of(ChallengeRankCalculatorProcessor.class);

	public ChallengeRankCalculatorProcessor() {
		super(EntityType.CHALLENGE.getIndexType(), EntityType.CHALLENGE
				.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		Event event = (Event) consumable;
		ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
				.fetchEventDetails();
		if (details == null) {
			return Status.NOT_CONSUMABLE;
		}
		DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID,
				details.id);

		boolean process = true;
		int start = 0;
		int rank = 0;
		long previousRanker = -1;
		List<String> topperIds = new ArrayList<String>();
		while (process) {

			List<ChallengeLeaderBoard> challengesLeaderBoards = ChallengeLeaderBoardDAO.INSTANCE
					.getInfos(query, null, start,
							ChallengeDAO.DEFAULT_BATCH_SIZE,
							new BasicDBObject(ConstantsGlobal.RANKER,
									SortOrder.ASC.getValue())).results;
			start += challengesLeaderBoards.size();
			if (challengesLeaderBoards.size() > 0) {
				for (ChallengeLeaderBoard challengeLeaderBoard : challengesLeaderBoards) {
					if (previousRanker != challengeLeaderBoard.ranker) {
						rank++;
					}
					challengeLeaderBoard.rank = rank;
					if (challengeLeaderBoard.rank == 1) {
						topperIds.add(challengeLeaderBoard.userId);
					}
					previousRanker = challengeLeaderBoard.ranker;
					ChallengeLeaderBoardDAO.INSTANCE.save(challengeLeaderBoard);
					LOGGER.info("saved challengeLeaderBoard["
							+ challengeLeaderBoard.challengeId + "] rank["
							+ rank + "] for user["
							+ challengeLeaderBoard.userId + "]");
				}
				start = challengesLeaderBoards.size();
			} else {
				process = false;
			}
		}

		if (CollectionUtils.isNotEmpty(topperIds)) {
			try {
				Challenge challenge = ChallengeDAO.INSTANCE
						.getChallenge(details.id);
				if (challenge == null) {
					LOGGER.error("no challenge found for challengeId: "
							+ details.id);
					return Status.FAILURE;
				}
				LOGGER.info("saving topperIds[" + topperIds
						+ "], for challenge[" + challenge._getStringId() + "]");
				challenge.topperIds = topperIds;
				ChallengeDAO.INSTANCE.save(challenge);
				details.fromMongoModel(challenge);
			} catch (VedantuException e) {
				LOGGER.error(e.getMessage(), e);
			}
			return super.process(consumable, details, false);
		}
		return Status.SUCCESS;
	}
}
