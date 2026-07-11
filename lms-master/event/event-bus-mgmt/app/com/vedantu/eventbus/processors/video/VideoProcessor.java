package com.vedantu.eventbus.processors.video;

import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class VideoProcessor extends ChainedProcessors implements IProcessor {

	// private static final ALogger LOGGER = Logger.of(TestProcessor.class);

	public VideoProcessor() {
		super(new ChainedProcessor(new VideoSearchIndexProcessor(), false)
		/*
		 * , new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
		 * new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, true )
		 */
		);
	}

	@Override
	public Status process(IConsumable consumable) {
		return super.process(consumable);
	}
}
