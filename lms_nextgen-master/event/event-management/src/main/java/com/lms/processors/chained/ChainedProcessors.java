package com.lms.processors.chained;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.utils.Consumer;

public class ChainedProcessors implements IProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ChainedProcessors.class);
	private ChainedProcessor[] processors;
      
	public void  setChainedProcessors(ChainedProcessor... processors) {
		this.processors = processors;

	}

	@Override
	public Status process(IConsumable consumable) {
		Status status = Status.FAILURE;
		if (null != processors && processors.length > 0) {
			for (ChainedProcessor processor : processors) {
				logger.info("using chainedProcessor : " + processor.getProcessorClass());
				status = processor.process(consumable);
				logger.info("status : " + status);
				if (status != Status.SUCCESS && processor.shouldReturnOnFailure()) {
					logger.info("returning on failure");
					return status;
				}
			}
			status = Status.SUCCESS;
		} else {
			logger.error("no processors found in chained processor : " + this.getClass());
		}
		return status;
	}
}
