package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.AssignmentSearchIndexDetails;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class AssignmentSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger.of(AssignmentSearchIndexProcessor.class);

    public AssignmentSearchIndexProcessor() {

        super(EntityType.ASSIGNMENT.getIndexType(), EntityType.ASSIGNMENT.getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {

        LOGGER.debug("assignment details object is : " + details + " and loaded details : "
                + loadedDetails);
        return super.process(consumable, new AssignmentSearchIndexDetails());
    }

}
