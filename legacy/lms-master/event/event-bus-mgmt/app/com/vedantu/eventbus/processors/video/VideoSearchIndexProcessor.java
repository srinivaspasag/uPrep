package com.vedantu.eventbus.processors.video;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.VideoSearchIndexDetails;
import com.vedantu.eventbus.processors.AbstractSearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class VideoSearchIndexProcessor extends AbstractSearchIndexProcessor {
	private static final ALogger	LOGGER	= Logger.of(VideoSearchIndexProcessor.class);

	public VideoSearchIndexProcessor() {
		super(EntityType.VIDEO.getIndexType(), EntityType.VIDEO.getIndexName());
	}

	@Override
	public Status process(IConsumable consumable) {
		LOGGER.debug("video details object is : " + details
				+ " and loaded details : " + loadedDetails);
		return super.process(consumable, new VideoSearchIndexDetails());
	}

}
