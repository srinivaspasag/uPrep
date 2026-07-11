package com.vedantu.eventbus.processors.chained;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class ChainedProcessors implements IProcessor {
	private static final ALogger LOGGER = Logger.of(ChainedProcessors.class);
	private ChainedProcessor[] processors;

	public ChainedProcessors(ChainedProcessor... processors) {
		this.processors = processors;

	}

	@Override
	public Status process(IConsumable consumable) {
		Status status = Status.FAILURE;
		if (null != processors && processors.length > 0) {
			for (ChainedProcessor processor : processors) {
				LOGGER.info("using chainedProcessor : "
						+ processor.getProcessorClass());
				status = processor.process(consumable);
				LOGGER.info("status : " + status);
				if (status != Status.SUCCESS
						&& processor.shouldReturnOnFailure()) {
					LOGGER.info("returning on failure");
					return status;
				}
			}
			status = Status.SUCCESS;
		} else {
			LOGGER.error("no processors found in chained processor : "
					+ this.getClass());
		}
		return status;
	}
}
