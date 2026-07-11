package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.events.Events;
import com.vedantu.eventbus.utils.FailedEventConsumer;
import com.vedantu.eventbus.utils.Producer;
import com.vedantu.eventbus.utils.Task;
import com.vedantu.events.utils.EventEnqueueManager;
import com.vedantu.events.utils.daemons.EventThreadManager;

public class EventBusProcessors extends AbstractVedantuController {
	private static final ALogger LOGGER = Logger.of(EventBusProcessors.class);

	public static Result startAll() {
		LOGGER.info("invoking eventThreadManager");
		EventThreadManager eventThreadManager = EventThreadManager
				.getInstance();
		Set<EventType> startedEventTypes = eventThreadManager.startAll();

		LOGGER.info("=========== eventThreadManager started ===============");
		return ok(getResultResponse(getSortedList(startedEventTypes))
				.toObjectNode());
	}

	public static Result stopAll() {
		LOGGER.info("======= trying to stop eventThreadManager =========");
		EventThreadManager eventThreadManager = EventThreadManager
				.getInstance();
		Set<EventType> stoppedEventTypes = eventThreadManager.stopAll();
		LOGGER.info("eventThreadManager stopped successfully");
		return ok(getResultResponse(getSortedList(stoppedEventTypes))
				.toObjectNode());
	}
	public static Result restartAll() {
		EventBusProcessors.stopAll();
		EventBusProcessors.startAll();
		EventThreadManager threadManager = EventThreadManager.getInstance();
		Map<EventType, Boolean> taskStatusMap = new HashMap<EventType, Boolean>();

		for (EventType e : getSortedList(Arrays.asList(EventType.values()))) {
			taskStatusMap.put(e, threadManager.status(e));
		}
		return ok(getResultResponse(taskStatusMap).toObjectNode());
	}
	public static Result start(String eventType) {

		EventType eType = EventType
				.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			LOGGER.error("invalid eventType : " + eventType);
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE,
							"invalid eventType : " + eventType)).toObjectNode());
		}

		EventThreadManager threadManager = EventThreadManager.getInstance();
		boolean started = threadManager.start(eType);
		return ok(getResultResponse(started).toObjectNode());
	}

	public static Result stop(String eventType) {

		EventType eType = EventType
				.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			LOGGER.error("invalid eventType : " + eventType);
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE,
							"invalid eventType : " + eventType)).toObjectNode());
		}

		EventThreadManager threadManager = EventThreadManager.getInstance();
		boolean stopped = threadManager.stop(eType);
		return ok(getResultResponse(stopped).toObjectNode());
	}

	public static Result getStatus(String eventType) {

		EventType eType = EventType
				.getEventTypeByValue(eventType.toUpperCase());
		if (EventType.UNKNOWN == eType) {
			LOGGER.error("invalid eventType : " + eventType);
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE,
							"invalid eventType : " + eventType)).toObjectNode());
		}
		EventThreadManager threadManager = EventThreadManager.getInstance();
		boolean running = threadManager.status(eType);
		return ok(getResultResponse(running).toObjectNode());
	}

	public static Result getStatusAll() {

		EventThreadManager threadManager = EventThreadManager.getInstance();
		Map<EventType, Boolean> taskStatusMap = new HashMap<EventType, Boolean>();

		for (EventType e : getSortedList(Arrays.asList(EventType.values()))) {
			taskStatusMap.put(e, threadManager.status(e));
		}
		return ok(getResultResponse(taskStatusMap).toObjectNode());
	}

	static Task<Events> task = null;

	@SuppressWarnings("unchecked")
	public static Result enqueeFailedEvents(int size) {
		if (size < 1) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
					.toObjectNode());
		}
		LOGGER.info("batch size : " + size);
		LOGGER.info("moving failedEvents for re-processing");
		size = Math.max(size, 10);
		Producer producer = new Producer(
				EventEnqueueManager.BUCKET_NAME_FAILURE, size);
		FailedEventConsumer consumer = new FailedEventConsumer();
		task = new Task<Events>("failedEvents", producer, consumer, 1, 60000);
		return ok(getResultResponse(task.isRunning()).toObjectNode());
	}

	public static Result stopEnqueeFailedEvents() {
		if (task == null) {
			LOGGER.info("no process is running for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE
					+ " and task is null");
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.NO_TASK_RUNNING))
					.toObjectNode());
		}
		boolean running = task.isRunning();
		if (running) {
			LOGGER.info("stoping process for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE);
			task.stop();
			running = !task.isRunning();
			LOGGER.info("stoped process for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE
					+ " re-processing");
		} else {
			LOGGER.info("no process is running for "
					+ EventEnqueueManager.BUCKET_NAME_FAILURE);
		}
		return ok(getResultResponse(running).toObjectNode());
	}

	private static List<EventType> getSortedList(
			Collection<EventType> eventTypes) {
		List<EventType> sortedEventTypes = new ArrayList<EventType>(eventTypes);
		Collections.sort(sortedEventTypes);
		return sortedEventTypes;
	}
}
