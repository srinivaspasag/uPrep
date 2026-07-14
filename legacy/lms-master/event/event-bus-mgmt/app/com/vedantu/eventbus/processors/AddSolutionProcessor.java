package com.vedantu.eventbus.processors;

import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.eventbus.processors.entities.EntitySearchIndexProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class AddSolutionProcessor extends ChainedProcessors {

    public AddSolutionProcessor() {

        super(new ChainedProcessor(EntitySearchIndexProcessor.INSTANCE, false),
                new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
                new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false));
    }

    @Override
    public Status process(IConsumable consumable) {

        return super.process(consumable);
    }

}
