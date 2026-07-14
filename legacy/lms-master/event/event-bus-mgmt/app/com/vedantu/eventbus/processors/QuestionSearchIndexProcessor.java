package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class QuestionSearchIndexProcessor extends AbstractSearchIndexProcessor {
	private static final ALogger LOGGER = Logger
			.of(QuestionSearchIndexProcessor.class);

	public QuestionSearchIndexProcessor() {
		super(EntityType.QUESTION.getIndexType(), EntityType.QUESTION
				.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		LOGGER.debug("question details object is : " + details
				+ " and loaded details : " + loadedDetails);
		return super.process(consumable, new QuestionSearchIndexDetails());
	}

}
