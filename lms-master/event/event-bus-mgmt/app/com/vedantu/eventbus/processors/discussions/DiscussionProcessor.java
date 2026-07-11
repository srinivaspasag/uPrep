package com.vedantu.eventbus.processors.discussions;

import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class DiscussionProcessor extends ChainedProcessors {

	public DiscussionProcessor() {
		super(new ChainedProcessor(new DiscussionSearchIndexProcessor(), false)
		/*
		 * new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, true ),
		 * new ChainedProcessor(OrganizationActivityTickerProcessor.INSTANCE,
		 * false)
		 */
		);
	}

}
