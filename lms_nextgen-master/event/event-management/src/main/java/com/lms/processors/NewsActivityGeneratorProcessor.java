package com.lms.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.enums.UserActionType.EventActionType;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.common.vedantu.mongo.Event;
import com.lms.managers.NewsActivityGenerator;
@Component
public class NewsActivityGeneratorProcessor implements IProcessor {

	private static final Logger logger = LoggerFactory.getLogger(NewsActivityGeneratorProcessor.class);
	public static final NewsActivityGeneratorProcessor INSTANCE = new NewsActivityGeneratorProcessor();
    @Autowired
	private NewsActivityGenerator newsActivityGenerator;
	private NewsActivityGeneratorProcessor() {

	}

	@SuppressWarnings("finally")
	@Override
	public Status process(IConsumable e) {

		boolean newsGenerationResult = false;
		try {
			Event event = (Event) e;
			if (event.action == EventActionType.REMOVE) {
				logger.info("as event action is remove hence not processing the event for newsActivity generation : "
						+ e._getConsumableId());
				return Status.SUCCESS;
			}
			logger.info("processing Event for " + event.getType() + " process for userId :" + event.getUserId());

			NewsActivity newsActivity = null;
			logger.info("fetching eventDetails" + event.fetchEventDetails());
			IEventDetails details = event.fetchEventDetails();

			if (details != null) {

				logger.debug("calling toNewsActivity");
				newsActivity = details.toNewsActivity();
				logger.debug("called toNewsActivity");
			}
			if (null == newsActivity) {
				logger.info(details + " newsActivity is null for an event" + event.getType());
				return Status.FAILURE;
			} else {
				// add details
				newsActivity.eType = event.getType();
				newsActivity.time = event.timeCreated;
			}
			logger.info("newsActivity : " + newsActivity);

			newsGenerationResult = newsActivityGenerator.generate(newsActivity, details);

			logger.info("result : " + newsGenerationResult);
		} catch (Throwable exception) {
			logger.info("News generation result : " + newsGenerationResult);

			logger.error("News Generation failed: ", exception);

		} finally {
			return newsGenerationResult ? Status.SUCCESS : Status.FAILURE;
		}
	}
}
