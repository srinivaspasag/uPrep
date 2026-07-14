package com.lms.utils;

import com.lms.common.utils.EventEnqueueManager;
import com.lms.common.utils.EventUtil;
import com.lms.common.vedantu.event.api.AbstractConsumer;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.mongo.Event;
import com.lms.events.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class FailedEventConsumer extends AbstractConsumer {
	@Autowired
	private EventEnqueueManager eventEnqueueManager;

	@Override
	protected void process(IConsumable consumable) {
		Events events = (Events) consumable;
		if (null != events) {
			for (Event event : events.getEvents()) {
				processEvent(event);

			}
		}

	}

	protected void processEvent(Event event) {
		try {
			eventEnqueueManager.enqueue(event);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logInfo(event, "event moved to : " + EventUtil.getEventQueueName(event.getType()));
		remove(event, EventEnqueueManager.BUCKET_NAME_FAILURE);

	}

}
