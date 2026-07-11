package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.FileSearchIndexDetails;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class FileSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger.of(FileSearchIndexProcessor.class);

    public FileSearchIndexProcessor() {

        super(EntityType.FILE.getIndexType(), EntityType.FILE.getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {

        LOGGER.debug("file details object is : " + details + " and loaded details : "
                + loadedDetails);
        return super.process(consumable, new FileSearchIndexDetails());
    }

}
