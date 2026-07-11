package com.vedantu.eventbus.processors.challenges;

import java.util.Arrays;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.models.Question;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.eventbus.processors.QuestionSearchIndexProcessor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class PublishChallengeEntityProcessor implements IProcessor {
	private static final ALogger LOGGER = Logger
			.of(PublishChallengeEntityProcessor.class);

	@Override
	public Status process(IConsumable consumable) {
		Event event = (Event) consumable;
		ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
				.fetchEventDetails();
		if (details == null) {
			LOGGER.error("null details ");
			return Status.NOT_CONSUMABLE;
		}
		for (SrcEntity entity : details.entities) {
			if (entity.type == EntityType.QUESTION) {
				if (details.publishType == Scope.PUBLIC) {
					LOGGER.info("entity : " + entity
							+ " changing it's scope to :" + details.publishType);
					try {
						Question q = QuestionDAO.INSTANCE
								.getQuestion(entity.id);
						if (q == null) {
							LOGGER.error("no question found corresponding to qid: "
									+ entity.id);
							// make this event stop here
							continue;
						}
						q.scope = details.publishType;
						q.challengeId = details.id;
						QuestionDAO.INSTANCE.updateModel(q, Arrays.asList(
								ConstantsGlobal.SCPOE,
								ConstantsGlobal.CHALLENGE_ID));
						QuestionSearchIndexDetails quesDetails = new QuestionSearchIndexDetails();
						quesDetails.fromMongoModel(q);
						quesDetails.setAction(EventActionType.UPDATE.name());
						QuestionSearchIndexProcessor qusProcessor = new QuestionSearchIndexProcessor();
						qusProcessor.process(event, quesDetails, false);
					} catch (VedantuException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}
		return Status.SUCCESS;
	}

}
