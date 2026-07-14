package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.eventbus.managers.NewsActivityGenerator;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class NewsActivityGeneratorProcessor implements IProcessor {

    private static final ALogger                       LOGGER   = Logger.of(NewsActivityGenerator.class);
    public static final NewsActivityGeneratorProcessor INSTANCE = new NewsActivityGeneratorProcessor();

    private NewsActivityGeneratorProcessor() {

    }

    @SuppressWarnings("finally")
    @Override
    public Status process(IConsumable e) {

        boolean newsGenerationResult = false;
        try {
            Event event = (Event) e;
            if (event.action == EventActionType.REMOVE) {
                LOGGER.info("as event action is remove hence not processing the event for newsActivity generation : "
                        + e._getConsumableId());
                return Status.SUCCESS;
            }
            LOGGER.info("processing Event for " + event.getType() + " process for userId :"
                    + event.getUserId());

            NewsActivity newsActivity = null;
            LOGGER.info("fetching eventDetails" + event.fetchEventDetails());
            IEventDetails details = event.fetchEventDetails();

            if (details != null) {

                LOGGER.debug("calling toNewsActivity");
                newsActivity = details.toNewsActivity();
                LOGGER.debug("called toNewsActivity");
            }
            if (null == newsActivity) {
                LOGGER.info(details + " newsActivity is null for an event" + event.getType());
                return Status.FAILURE;
            } else {
                // add details
                newsActivity.eType = event.getType();
                newsActivity.time = event.timeCreated;
            }
            LOGGER.info("newsActivity : " + newsActivity);

            newsGenerationResult = NewsActivityGenerator.getInstance().generate(newsActivity,
                    details);

            LOGGER.info("result : " + newsGenerationResult);
        } catch (Throwable exception) {
            LOGGER.info("News generation result : " + newsGenerationResult);

            LOGGER.error("News Generation failed: ", exception);

        } finally {
            return newsGenerationResult ? Status.SUCCESS : Status.FAILURE;
        }
    }
}
