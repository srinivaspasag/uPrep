package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.models.event.search.details.CMDSModuleSearchIndexDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class CMDSModuleSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger
            .of(CMDSModuleSearchIndexProcessor.class);

    public CMDSModuleSearchIndexProcessor() {
        super(EntityType.CMDSMODULE.getIndexType(), EntityType.CMDSMODULE
                .getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {
        LOGGER.info("cmdsmodule details object is : " + details
                + " and loaded details : " + loadedDetails);
        return super.process(consumable, new CMDSModuleSearchIndexDetails());
    }

}
