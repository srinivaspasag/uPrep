package com.vedantu.eventbus.utils;

import com.vedantu.eventbus.events.Events;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.AbstractConsumer;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.utils.EventEnqueueManager;
import com.vedantu.events.utils.EventUtil;

public class FailedEventConsumer extends AbstractConsumer {

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
        EventEnqueueManager.enqueue(event);
        logInfo(event,
                "event moved to : "
                        + EventUtil.getEventQueueName(event.getType()));
        remove(event, EventEnqueueManager.BUCKET_NAME_FAILURE);

    }

}
