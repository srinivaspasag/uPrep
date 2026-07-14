package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.maintenance.managers.IndexingManager;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class ReIndexProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(ReIndexProcessor.class);

    public ReIndexProcessor() {

    }

    @Override
    public Status process(IConsumable consumable) {

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;
        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof ReIndexDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type " + ReIndexDetails.class);
            return Status.FAILURE;
        }

        ReIndexDetails reIndexDetails = (ReIndexDetails) details;
        boolean reIndexResult = false;
        try {
            reIndexResult = IndexingManager.INSTANCE.reIndex(reIndexDetails.type,
                    reIndexDetails.ids, reIndexDetails.userId, reIndexDetails.containerType,
                    reIndexDetails.linkType);
        } catch (VedantuException e) {
            LOGGER.error("Reindexing processor failed", e);
        }
        if (reIndexResult) {
            return Status.SUCCESS;
        }
        return Status.FAILURE;
    }
}
