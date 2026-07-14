package com.vedantu.eventbus.processors.entities;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.PointIncrementerProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;

public class CommentProcessor extends AbstractSocialActionProcessor {

    public CommentProcessor() {

        super(new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
                new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false));
    }

}
