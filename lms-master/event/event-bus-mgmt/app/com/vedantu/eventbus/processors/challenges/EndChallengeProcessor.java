package com.vedantu.eventbus.processors.challenges;

import java.util.Arrays;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class EndChallengeProcessor extends AbstractSearchIndexProcessor {

	private static final ALogger LOGGER = Logger
			.of(EndChallengeProcessor.class);

	public EndChallengeProcessor() {
		super(EntityType.CHALLENGE.getIndexType(), EntityType.CHALLENGE
				.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		Event event = (Event) consumable;
		ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
				.fetchEventDetails();
		Challenge challenge = null;
		try {
			challenge = ChallengeDAO.INSTANCE.getChallenge(details.id);
		} catch (VedantuException e) {
			LOGGER.error(e.getMessage(), e);
		}
		if (challenge == null) {
			LOGGER.error("no challenge found for challengeId: " + details.id);
			return Status.FAILURE;
		}
		try {
			if (!endChallenge(challenge)) {
				return Status.FAILURE;
			}
		} catch (VedantuException e) {
			LOGGER.error(e.getMessage(), e);
			return Status.FAILURE;
		}
		details.fromMongoModel(challenge);
		return super.process(consumable, details, false);
	}

	private boolean endChallenge(Challenge challenge) throws VedantuException {
		LOGGER.info("ending challenge[" + challenge._getStringId() + "]");
		challenge.status = ChallengeStatus.ENDED;
		ChallengeDAO.INSTANCE.updateModel(challenge,
				Arrays.asList(ConstantsGlobal.STATUS));
		LOGGER.info("successfully marked challenge[" + challenge._getStringId()
				+ "] ended in mongo");
		return true;
	}
}
