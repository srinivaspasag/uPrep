package com.lms.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.common.vedantu.mongo.Event;
import com.lms.utils.PointIncrementer;
@Component
public class PointIncrementerProcessor implements IProcessor {

	private static final Logger logger = LoggerFactory.getLogger(PointIncrementerProcessor.class);
	public static PointIncrementerProcessor INSTANCE = new PointIncrementerProcessor();
    @Autowired
	private PointIncrementer pointIncrementer;
	private PointIncrementerProcessor() {
	}

	@Override
	public Status process(IConsumable e) {
		Event event = (Event) e;
		logger.info("processing Event for " + this.getClass() + " process for userId :" + event.getUserId()
				+ " and eventId: " + event._getStringId());
		PointIncrementer pointIncrementer1 = pointIncrementer; //PointIncrementer.getInstance();
		logger.info("increment points ");
		pointIncrementer1.incrementPoint(event);
		return Status.SUCCESS;
	}

}
