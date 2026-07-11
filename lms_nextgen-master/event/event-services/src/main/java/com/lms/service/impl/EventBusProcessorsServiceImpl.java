package com.lms.service.impl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EventEnqueueManager;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EventType;
import com.lms.events.Events;
import com.lms.service.EventBusProcessorsService;
import com.lms.utils.FailedEventConsumer;
import com.lms.utils.Producer;
import com.lms.utils.Task;
import com.lms.utils.daemons.EventThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventBusProcessorsServiceImpl implements EventBusProcessorsService {
	private static final Logger logger = LoggerFactory.getLogger(EventBusProcessorsServiceImpl.class);
	@Autowired
	private EventThreadManager eventThreadManager;
	@Autowired
	private Task<Events> task;
	@Autowired
	private Producer producer;
	@Autowired
	private FailedEventConsumer consumer;

	@Override
	public VedantuResponse startAll() {
		logger.info("invoking eventThreadManager");
		eventThreadManager.eventThreadManager();
		Set<EventType> startedEventTypes = eventThreadManager.startAll();

		logger.info("=========== eventThreadManager started ===============");
		return new VedantuResponse(getSortedList(startedEventTypes));

	}
	private static List<EventType> getSortedList(
			Collection<EventType> eventTypes) {
		List<EventType> sortedEventTypes = new ArrayList<EventType>(eventTypes);
		Collections.sort(sortedEventTypes);
		return sortedEventTypes;
	}
	@Override
	public VedantuResponse stopAll() {
		logger.info("======= trying to stop eventThreadManager =========");
		eventThreadManager.eventThreadManager();
		Set<EventType> stoppedEventTypes = eventThreadManager.stopAll();
		logger.info("eventThreadManager stopped successfully");
		return new VedantuResponse(getSortedList(stoppedEventTypes));
	}
	@Override
	public VedantuResponse restartAll() {
		stopAll();
		startAll();
		Map<EventType, Boolean> taskStatusMap = new HashMap<EventType, Boolean>();

		for (EventType e : getSortedList(Arrays.asList(EventType.values()))) {
			taskStatusMap.put(e, eventThreadManager.status(e));
		}
		return new VedantuResponse(taskStatusMap);
	}
	@Override
	public VedantuResponse start(String eventType) {
		EventType eType = EventType.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			logger.error("invalid eventType : " + eventType);
			throw new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE, "invalid eventType : " + eventType);
		}
		eventThreadManager.eventThreadManager();
		boolean started = eventThreadManager.start(eType);
		return new VedantuResponse(started);

	}

	@Override
	public VedantuResponse stop(String eventType) {
		EventType eType = EventType.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			logger.error("invalid eventType : " + eventType);
			throw new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE, "invalid eventType : " + eventType);
		}

		boolean stopped = eventThreadManager.stop(eType);
		return new VedantuResponse(stopped);
	}

	@Override
	public VedantuResponse getStatus(String eventType) {
		EventType eType = EventType
				.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			logger.error("invalid eventType : " + eventType);
			throw new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE, "invalid eventType : " + eventType);
		}
		boolean running = eventThreadManager.status(eType);
		return new VedantuResponse(running);
	}

	@Override
	public VedantuResponse getStatusAll() {
		Map<EventType, Boolean> taskStatusMap = new HashMap<EventType, Boolean>();

		for (EventType e : getSortedList(Arrays.asList(EventType.values()))) {
			taskStatusMap.put(e, eventThreadManager.status(e));
		}
		return new VedantuResponse(taskStatusMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public VedantuResponse enqueeFailedEvents(int size) {
		if (size < 1) {
			throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

		}
		logger.info("batch size : " + size);
		logger.info("moving failedEvents for re-processing");
		size = Math.max(size, 10);
		producer.producer(EventEnqueueManager.BUCKET_NAME_FAILURE, size);

		task.task("failedEvents", producer, consumer, 1, 60000);
		return new VedantuResponse(task.isRunning());

	}

	@Override
	public VedantuResponse stopEnqueeFailedEvents() {
		if (task == null) {
			logger.info("no process is running for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE
					+ " and task is null");
			throw new VedantuException(VedantuErrorCode.NO_TASK_RUNNING);
		}
		boolean running = task.isRunning();
		if (running) {
			logger.info("stoping process for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE);
			task.stop();
			running = !task.isRunning();
			logger.info("stoped process for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE
					+ " re-processing");
		} else {
			logger.info("no process is running for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE);
		}
		return new VedantuResponse(running);

	}

}
