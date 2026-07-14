package com.vedantu.eventbus.processors.challenges;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class ChallengeProcessor extends ChainedProcessors {

    public ChallengeProcessor() {

        super(new ChainedProcessor(new ChallengeSearchIndexProcessor(), false),
                new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false));
    }
}
