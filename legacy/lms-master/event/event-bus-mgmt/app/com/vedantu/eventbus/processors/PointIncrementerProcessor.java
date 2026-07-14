package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.eventbus.utils.PointIncrementer;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class PointIncrementerProcessor implements IProcessor {

	private static final ALogger LOGGER = Logger
			.of(PointIncrementerProcessor.class);
	public static PointIncrementerProcessor INSTANCE = new PointIncrementerProcessor();

	private PointIncrementerProcessor() {
	}

	@Override
	public Status process(IConsumable e) {
		Event event = (Event) e;
		LOGGER.info("processing Event for " + this.getClass()
				+ " process for userId :" + event.getUserId()
				+ " and eventId: " + event._getStringId());
		PointIncrementer pointIncrementer = PointIncrementer.getInstance();
		LOGGER.info("increment points ");
		pointIncrementer.incrementPoint(event);
		return Status.SUCCESS;
	}

}
