package com.lms.common.utils;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.mongo.Event;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class EventUtil {
    private static final Logger logger = LoggerFactory.getLogger(EventUtil.class);
    public static final String EVENTS = "events_";

    @Autowired
    private EventEnqueueManager eventEnqueueManager;

    public static String getEventQueueName(EventType eventType) {
        if (null == eventType) {
            logger.error("null eventType");
            throw new IllegalArgumentException("null eventType");
        }
        return new StringBuilder(EVENTS).append(eventType.name()).toString();
    }

    public String generateEvent(EventType eventType, String requestUrl,
                                String userId, IEventDetails details, SrcEntity srcEntity,
                                UserActionType.EventActionType action) {

        return generateEvent(eventType, requestUrl, userId, details, srcEntity,
                action, 0);
    }

    public String generateEvent(EventType eventType, String requestUrl,
                                String userId, IEventDetails details, SrcEntity srcEntity) {

        return generateEvent(eventType, requestUrl, userId, details, srcEntity,
                null, 0);
    }

    public String generateEvent(EventType eventType, String requestUrl,
                                String userId, IEventDetails details, SrcEntity srcEntity,
                                UserActionType.EventActionType action, long processTime) {
        String generatedEventId = null;
        // EventEnqueueManager eventEnqueueManager=new  EventEnqueueManager();

        try {
            if (eventType != null && userId != null) {
                logger.debug("creating event for : " + eventType
                        + " for userId: " + userId + " and source url is : "
                        + requestUrl);
                Event event = new Event(eventType, requestUrl, userId, details,
                        srcEntity);
                event.action = action;
                event.timeCreated = processTime != 0 ? processTime : System
                        .currentTimeMillis();
                try {
                    generatedEventId = eventEnqueueManager.enqueue(event);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logger.info("event[" + generatedEventId + "] got created for "
                        + eventType + " : for : " + userId + ", action:"
                        + action);
            } else {
                logger.error("can not create event for empty userId or eventType");
            }
        } catch (JSONException e) {
            logger.error("error on generating the event for : " + eventType, e);
        }
        return generatedEventId;
    }
}
