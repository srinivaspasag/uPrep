package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.models.event.search.details.CMDSFileSearchIndexDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class CMDSFileSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger.of(CMDSFileSearchIndexProcessor.class);

    public CMDSFileSearchIndexProcessor() {

        super(EntityType.CMDSFILE.getIndexType(), EntityType.CMDSFILE.getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {

        LOGGER.info("cmdsfile details object is : " + details + " and loaded details : "
                + loadedDetails);
        return super.process(consumable, new CMDSFileSearchIndexDetails());
    }

}
