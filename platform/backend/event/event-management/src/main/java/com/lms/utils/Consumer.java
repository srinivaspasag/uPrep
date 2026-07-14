package com.lms.utils;

import com.lms.common.utils.EventEnqueueManager;
import com.lms.common.utils.EventUtil;
import com.lms.common.vedantu.enums.Status;
import com.lms.common.vedantu.event.api.AbstractConsumer;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.common.vedantu.event.errors.TypeNotMatchedException;
import com.lms.common.vedantu.mongo.Event;
import com.lms.events.Events;
import com.lms.events.errors.ProcessorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

@Component
public class Consumer extends AbstractConsumer {

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
	private static final int MAX_TRIES = 3;
	@Autowired
	private ProcessorFactory processorFactory;
	private EventEnqueueManager eventEnqueueManager;

	public Consumer() {
		super();
		try {
			//MAX_TRIES = Integer.parseInt(Play.application().configuration().getString("max.tries"));
		} catch (Exception e) {
			logger.error("propery max.tries not found in application.conf file ", e);
		}
	}

	@Override
	protected void process(IConsumable consumable) {
		Events events = (Events) consumable;
		if (null != events) {
			for (Event event : events.getEvents()) {
				preProcessEvent(event);
				Status status = processEvent(event);
				events.setStatus(event, status);
			}
		}

	}

	protected void preProcessEvent(Event event) {
		// TODO: check if this function need to be implemented
		// if (event.srcEntity != null
		// && (event.getType() == EventType.FOLLOW_ENTITY || event
		// .getType() == EventType.ATTEMPT_ENTITY)) {
		// LOGGER.debug("preProcessing eventInfo: " + event.getInfo()
		// + ", action: " + event.action);
		// boolean upsert = event.action != null
		// && event.action != EventAction.REMOVE;
		// // TODO: remove this style of coding as now attempt leads to
		// // autoFollow
		// EntityUserAction entityUserAction = UserUtilCommon
		// .getEntityUserAction(event.getUserId(), event.srcEntity.id,
		// event.srcEntity.type, event.getType(), upsert);
		// if (upsert) {
		// LOGGER.debug("upsert[" + upsert + "] saving entityUserAction: "
		// + entityUserAction);
		// entityUserAction.saveNow();
		// } else if (!upsert && entityUserAction != null) {
		// LOGGER.debug("removing entityUserAction : " + entityUserAction);
		// entityUserAction.delete();
		// }
		// } else {
		// LOGGER.info("not saving entityUser action for srcEntity:"
		// + event.srcEntity);
		// }
	}

	protected Status processEvent(Event event) {
		Status status = Status.FAILURE;
		try {
			logInfo(event, "getting processor for the eventType- : " + event.getType());
			//ProcessorFactory pFactory = ProcessorFactory.getInstance();
			try {
				processorFactory.setProcessorFactory();
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProcessorFactory pFactory = processorFactory;
			IProcessor processor = pFactory.getProcessor(event.getType());
			logInfo(event, "process class is : " + processor.getClass());

			//event.lastTriedTime = System.currentTimeMillis();
			//event.nTries++;
			status = processor.process(event);

			logInfo(event, "event status : " + status);

		} catch (TypeNotMatchedException e) {
			logError(event, e.getMessage() + " for " + event.getType(), e);
		} catch (ProcessorNotFoundException e) {
			logError(event, e.getMessage() + " for " + event.getType(), e);
		}
		return status;
	}

	@Override
	protected void postProcess(IConsumable consumable) {

		Events events = (Events) consumable;
		if (null != events) {
			for (Event event : events.getEvents()) {
				boolean shouldAbort = postProcessEvent(event, events.getStatus(event));
				if (shouldAbort) {
					logInfo(event,
							"aborting the present queue as this event failed (requeued) " + event._getStringId());
					break;
				}
			}
		}
	}

	protected boolean postProcessEvent(Event event, Status status) {

		boolean shouldAbort = false;

		if (Status.SUCCESS != status) {

			if (event.nTries >= MAX_TRIES) {
				moveToFailureBucket(event, status);
			} else {
				logInfo(event, "updating maxTries for event " + event._getStringId());
				try {
					eventEnqueueManager.updateEvent(event);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logInfo(event, "saved event");
				shouldAbort = true;
			}

		} else {
			moveToSuccessBucket(event);
		}

		return shouldAbort;
	}

	private void moveToSuccessBucket(Event event) {

		logDebug(event, "moving event to sucess bucket");
		try {
			eventEnqueueManager.addToSuccessBucket(event);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logInfo(event, "event saved to sucess bucket");
		remove(event, EventUtil.getEventQueueName(event.getType()));
	}

	private void moveToFailureBucket(Event event, Status status) {
		logInfo(event, "moving event to failure bucket");
		try {
			eventEnqueueManager.addToFailedBucket(event);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logInfo(event, "saving failed event object to DB failure bucket");
		remove(event, EventUtil.getEventQueueName(event.getType()));
	}

}
