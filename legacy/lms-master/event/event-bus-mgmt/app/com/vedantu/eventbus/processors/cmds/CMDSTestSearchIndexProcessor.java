package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.models.event.search.details.CMDSTestSearchIndexDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class CMDSTestSearchIndexProcessor extends AbstractSearchIndexProcessor {

	private static final ALogger LOGGER = Logger
			.of(CMDSTestSearchIndexProcessor.class);

	public CMDSTestSearchIndexProcessor() {
		super(EntityType.CMDSTEST.getIndexType(), EntityType.CMDSTEST
				.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		LOGGER.info("cmdsquestion details object is : " + details
				+ " and loaded details : " + loadedDetails);
		return super.process(consumable, new CMDSTestSearchIndexDetails());
	}

}
