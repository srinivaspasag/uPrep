package com.vedantu.eventbus.processors;

import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.eventbus.processors.cmds.EntityPublishingProcessor;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class PublishProcessor extends ChainedProcessors {

    public PublishProcessor() {

        // @formatter:off
        super(
                new ChainedProcessor(new EntityPublishingProcessor(), true),
                new ChainedProcessor(new EntitySizeCalculatorProcessor(), true)
              
//             ,new ChainedProcessor(// new UpdateLibraryProcessor(), true)
              );
        // @formatter:on
    }

    @Override
    public Status process(IConsumable consumable) {

        return super.process(consumable);
    }

}
