package com.vedantu.eventbus.processors.challenges;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class PostChallengeProcessor extends ChainedProcessors {

    public PostChallengeProcessor() {
        super(new ChainedProcessor(new EndChallengeProcessor(), false),
                new ChainedProcessor(new ChallengeUserAttemptStatusProcessor(), false),
                new ChainedProcessor(new ChallengeLeaderBoardProcessor(), false),
                new ChainedProcessor(new ChallengeRankCalculatorProcessor(), false),
                new ChainedProcessor(new ChallengeCreditProcessor(), false),
                new ChainedProcessor(new ChallengePointProcessor(), false),
                new ChainedProcessor(new ChallengeMultiplierPowerProcessor(), false),
                new ChainedProcessor(new PublishChallengeEntityProcessor(), false),
                new ChainedProcessor( NewsActivityGeneratorProcessor.INSTANCE, false));
    }
}
