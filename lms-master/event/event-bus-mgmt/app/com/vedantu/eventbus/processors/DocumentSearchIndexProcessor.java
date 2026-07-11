package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.DocumentSearchIndexDetails;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class DocumentSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger.of(DocumentSearchIndexProcessor.class);

    public DocumentSearchIndexProcessor() {

        super(EntityType.DOCUMENT.getIndexType(), EntityType.DOCUMENT.getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {

        LOGGER.debug("document details object is : " + details + " and loaded details : "
                + loadedDetails);
        return super.process(consumable, new DocumentSearchIndexDetails());
    }

}
