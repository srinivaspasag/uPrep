package com.vedantu.eventbus.processors.cmds;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.eventbus.processors.NewsActivityGeneratorProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;

public class MadeVisibleProcessor extends ChainedProcessors {

    private static final ALogger LOGGER = Logger.of(MadeVisibleProcessor.class);

    public MadeVisibleProcessor() {

        super(new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, false));
                
                //new ChainedProcessor(new EntitySizeCalculatorProcessor(), false)
             //   new ChainedProcessor(new UpdateLibraryProcessor(), false));
    }

}
