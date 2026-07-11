package com.vedantu.eventbus.processors.comm;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class PostRemarksProcessor extends ChainedProcessors {

	public PostRemarksProcessor() {
		super(new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE,
				false));
	}
}
