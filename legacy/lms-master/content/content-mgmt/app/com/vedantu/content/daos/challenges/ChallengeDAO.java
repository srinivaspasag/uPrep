package com.vedantu.content.daos.challenges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.AbstractAttemptableDAO;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.challenges.BidType;
import com.vedantu.content.enums.challenges.ChallengeType;
import com.vedantu.content.models.challenges.Challenge;

public class ChallengeDAO extends AbstractAttemptableDAO<Challenge, ObjectId> {
	public static final int DEFAULT_BATCH_SIZE = 200;
	private static final ALogger LOGGER = Logger.of(ChallengeDAO.class);

	public static final ChallengeDAO INSTANCE = new ChallengeDAO();

	private ChallengeDAO() {
		super(Challenge.class);
	}

	public Challenge addChallenge(String userId, String name, int lifeTime,
			int duration, int maxBid, Scope publishType, Difficulty difficulty,
			Scope scope, List<String> hints, int initialBidPool,
			List<Integer> hintsDeduction, SrcEntity entity, Set<String> brdIds,
			Set<String> targetIds, QuestionType qType, Set<String> tags,
			SrcEntity contentSrc) {

		ChallengeType type = duration > 0 ? ChallengeType.FIXED_TIME
				: ChallengeType.NO_TIME;
		BidType bidType = maxBid <= 0 ? BidType.NON_BIDDABLE : BidType.BIDDABLE;

		Challenge challenge = new Challenge(userId, name, type, lifeTime,
				duration, scope, difficulty, bidType, maxBid, initialBidPool);
		challenge.contentSrc = contentSrc;
		challenge.hintsDeductionValues = hintsDeduction;
		challenge.qTypes = new HashSet<String>(Arrays.asList(qType.name()));
		challenge.publishType = publishType;
		challenge.tags = tags;
		challenge.addTargets(targetIds);
		challenge.addBoards(brdIds);
		challenge.addEntity(entity);
		challenge.addHint(hints);
		LOGGER.debug("saving challenge : " + challenge);
		save(challenge);
		return challenge;
	}

	public Challenge getChallenge(String id) throws VedantuException {
		Challenge challenge = getById(id);
		if (challenge == null) {
			throw new VedantuException(VedantuErrorCode.CHALLENGE_NOT_FOUND);
		}
		return challenge;
	}

}
