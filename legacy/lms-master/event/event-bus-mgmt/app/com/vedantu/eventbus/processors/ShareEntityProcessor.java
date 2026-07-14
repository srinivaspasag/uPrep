package com.vedantu.eventbus.processors;

import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class ShareEntityProcessor extends ChainedProcessors {
	public ShareEntityProcessor() {
        super(//new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
                new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false)
               // ,new ChainedProcessor(SharedWithMeTickerProcessor.INSTANCE, false)
                );
    }
}
