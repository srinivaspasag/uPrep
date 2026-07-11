package com.vedantu.eventbus.processors.comm;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.managers.news.NewsFeedManager;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.content.event.details.NewsRemoveDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class NewsRemoveProcessor implements IProcessor {

    private final static ALogger LOGGER = Logger.of(NewsRemoveProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        if (event.action == EventActionType.REMOVE) {
            Logger.info("as event action is remove hence not processing the event for newsActivity generation : "
                    + consumable._getConsumableId());
            return Status.SUCCESS;
        }
        Logger.info("processing Event for " + event.getType() + " process for userId :"
                + event.getUserId());
        NewsRemoveDetails details = (NewsRemoveDetails) event.fetchEventDetails();
        NewsFeedManager manager = new NewsFeedManager();
        try {
            manager.deleteAllNews(event.getUserId(), details.content);
        } catch (VedantuException e) {
            LOGGER.error("Removed news feeds failed", e);
            return Status.FAILURE;
        }
        if (CollectionUtils.isNotEmpty(details.newsFeedsForRemoval)) {

            try {
                manager.deleteNewsFeeds(details.newsFeedsForRemoval);
            } catch (VedantuException e) {
                LOGGER.debug("Removal news feeds failed");
            }

        }
        return Status.SUCCESS;
    }

}
