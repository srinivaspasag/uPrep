package com.vedantu.eventbus.processors.entities;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.PointIncrementerProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public abstract class AbstractSocialActionProcessor extends ChainedProcessors {

    public AbstractSocialActionProcessor() {

        this(new ChainedProcessor(EntitySearchIndexProcessor.INSTANCE, false),
                new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
                new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false));
    }

    public AbstractSocialActionProcessor(ChainedProcessor... processors) {

        super(processors);
    }

}
