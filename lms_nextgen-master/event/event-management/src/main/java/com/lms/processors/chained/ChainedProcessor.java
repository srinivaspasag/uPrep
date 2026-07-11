package com.lms.processors.chained;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IProcessor;
@Component
public class ChainedProcessor implements IProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ChainedProcessor.class);
	private IProcessor processor;
	private boolean shouldReturnOnFailure;

	public void setChainedProcessor(IProcessor processor, boolean shouldReturnOnFailure) {
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
			status = processedBy.contains(processor.getClass().getName()) ? Status.SUCCESS : Status.FAILURE;
		}
		if (status != Status.SUCCESS) {
			status = processor.process(consumable);
		} else {
			logger.info("event already processed by : " + processor.getClass().getName());
		}
		if (status == Status.SUCCESS) {
			logger.info("event processed by : " + processor.getClass().getName()
					+ " adding processor to processedBy Field");
			consumable.addProcessedBy(processor.getClass().getName());
		}
		return status;
	}

	public boolean shouldReturnOnFailure() {
		return shouldReturnOnFailure;
	}

}
