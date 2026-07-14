package com.lms.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.processors.chained.ChainedProcessor;
import com.lms.processors.chained.ChainedProcessors;
@Component
public class AddSolutionProcessor extends ChainedProcessors {
	@Autowired
    private PointIncrementerProcessor pointIncrementerProcessor;
	@Autowired
	private NewsActivityGeneratorProcessor newsActivityGeneratorProcessor;
	@Autowired
	private ChainedProcessor chainedProcessor;
	@Autowired
	private ChainedProcessor chainedProcessor1;
	public  void setAddSolutionProcessor() {
  
		/*super(new ChainedProcessor(pointIncrementerProcessor, false),
				new ChainedProcessor(newsActivityGeneratorProcessor, false));*/
		chainedProcessor.setChainedProcessor(pointIncrementerProcessor, false);
		chainedProcessor1.setChainedProcessor(newsActivityGeneratorProcessor, false);
		setChainedProcessors(chainedProcessor1,chainedProcessor);
	}

	@Override
	public Status process(IConsumable consumable) {

		return super.process(consumable);
	}

}
