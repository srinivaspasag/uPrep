package com.vedantu.eventbus.processors.chained;

import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class ChainedProcessor implements IProcessor {
	private static final ALogger LOGGER = Logger.of(ChainedProcessor.class);
	private IProcessor processor;
	private boolean shouldReturnOnFailure;

	public ChainedProcessor(IProcessor processor, boolean shouldReturnOnFailure) {
		this.processor = processor;
		this.shouldReturnOnFailure = shouldReturnOnFailure;
	}

	public Class<?> getProcessorClass() {
		return null != processor ? processor.getClass() : null;
	}

	@Override
	public Status process(IConsumable consumable) {
		Status status = Status.FAILURE;
		Set<String> processedBy = consumable._getProcessedBy();
		if (processedBy != null && !processedBy.isEmpty()) {
			status = processedBy.contains(processor.getClass().getName()) ? Status.SUCCESS
					: Status.FAILURE;
		}
		if (status != Status.SUCCESS) {
			status = processor.process(consumable);
		} else {
			LOGGER.info("event already processed by : "
					+ processor.getClass().getName());
		}
		if (status == Status.SUCCESS) {
			LOGGER.info("event processed by : "
					+ processor.getClass().getName()
					+ " adding processor to processedBy Field");
			consumable.addProcessedBy(processor.getClass().getName());
		}
		return status;
	}

	public boolean shouldReturnOnFailure() {
		return shouldReturnOnFailure;
	}

}
