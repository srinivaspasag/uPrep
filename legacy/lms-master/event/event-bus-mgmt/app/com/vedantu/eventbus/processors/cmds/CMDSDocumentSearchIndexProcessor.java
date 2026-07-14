package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.models.event.search.details.CMDSDocumentSearchIndexDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class CMDSDocumentSearchIndexProcessor extends AbstractSearchIndexProcessor {

    private static final ALogger LOGGER = Logger.of(CMDSDocumentSearchIndexProcessor.class);

    public CMDSDocumentSearchIndexProcessor() {

        super(EntityType.CMDSDOCUMENT.getIndexType(), EntityType.CMDSDOCUMENT.getIndexName());
    }

    @Override
    public Status process(IConsumable consumable) {

        LOGGER.info("cmdsquestion details object is : " + details + " and loaded details : "
                + loadedDetails);
        return super.process(consumable, new CMDSDocumentSearchIndexDetails());
    }

}
