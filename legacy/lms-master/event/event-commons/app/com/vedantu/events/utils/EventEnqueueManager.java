package com.vedantu.events.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.events.models.Event;
import com.vedantu.mongo.MorphiaManager;

public class EventEnqueueManager {

	private static final ALogger LOGGER = Logger.of(EventEnqueueManager.class);

	private static ObjectMapper mapper;
	private static final String TYPE = "type";
	private static final String SOURCE = "source";
	private static final String INFO = "info";
	private static final String USER_ID = "userId";
	private static final String SRC_ENTITY = "srcEntity";
	private static final String N_TRIES = "nTries";
	private static final String LAST_TRIED_TIME = "lastTriedTime";
	private static final String PROCESSED_BY = "processedBy";
	private static final String ACTION = "action";

	private static Set<String> collections = new HashSet<String>();

	public static String enqueue(Event event) {
		return enqueueOrUpdate(event, null, false);
	}

	public static String updateEvent(Event event) {
		return enqueueOrUpdate(event, null, true);
	}

	public static final String BUCKET_NAME_SUCCESS = "consumed_EVENTS";
	public static final String BUCKET_NAME_FAILURE = "failed_EVENTS";

	public static void addToSuccessBucket(Event event) {
		enqueueOrUpdate(event, BUCKET_NAME_SUCCESS, false);
	}

	public static void addToFailedBucket(Event event) {
		enqueueOrUpdate(event, BUCKET_NAME_FAILURE, false);
	}

	private static String enqueueOrUpdate(Event event, String collectionName,
			boolean isUpdate) {
		EventType eventType = event.getType();
		if (!EventType.isValidEventType(eventType.name())) {
			LOGGER.error("unknown event type : " + eventType);
			return null;
		}

		final String opType = (isUpdate ? "update" : "enqueue");
		LOGGER.debug("trying to " + opType + " event");
		DB db = MorphiaManager.INSTANCE.getDS().getDB();
		final String queueCollectionName = StringUtils.isEmpty(collectionName) ? EventUtil
				.getEventQueueName(eventType) : collectionName;
		DBObject eventDBObj = getEventDBObject(event);
		DBCollection coll = null;
		boolean present = collections.contains(queueCollectionName);
		if (!present) {
			present = createCollection(db, queueCollectionName, eventType,
					eventDBObj);
		}

		if (present) {
			coll = db.getCollection(queueCollectionName);
			if (isUpdate) {
				coll.update(new BasicDBObject("_id", event.id), eventDBObj);
			} else {
				coll.insert(eventDBObj);
			}
			LOGGER.info(opType + " successful");
		}
		String eventId = eventDBObj.get(ConstantsGlobal._ID) != null ? eventDBObj
				.get(ConstantsGlobal._ID).toString() : null;
		LOGGER.info("generated eventId is : " + eventId);
		db.requestDone();
		return eventId;
	}

	private synchronized static boolean createCollection(final DB db,
			final String collectionName, final EventType eventType,
			final DBObject eventDBObj) {
		boolean present = db.collectionExists(collectionName);
		if (!present) {
			LOGGER.debug("creating collection " + collectionName
					+ " for event ");
			try {
				db.createCollection(collectionName, eventDBObj);
			} catch (Exception e) {
				LOGGER.error("collection already exist", e);
			}
			present = db.collectionExists(collectionName);
		} else {
			LOGGER.debug("collection present : " + collectionName);
		}
		if (!collections.contains(collectionName)) {
			collections.add(collectionName);
		}
		return present;
	}

	private static DBObject getEventDBObject(Event event) {

		DBObject eventDBObj = new BasicDBObject();
		eventDBObj.put(TYPE, event.getType().name());
		eventDBObj.put(SOURCE, event.getSource());
		eventDBObj.put(ConstantsGlobal.TIME_CREATED, event.timeCreated);
		if (event._getInfo() != null) {
			eventDBObj.put(INFO, event._getInfo().toString());
		}
		eventDBObj.put(USER_ID, event.getUserId());
		eventDBObj.put(SRC_ENTITY,
				mapper().convertValue(event.srcEntity, Map.class));
		eventDBObj.put(N_TRIES, event.nTries);
		eventDBObj.put(LAST_TRIED_TIME, event.lastTriedTime);
		eventDBObj.put(PROCESSED_BY, event.processedBy);
		if (event.action != null) {
			eventDBObj.put(ACTION, event.action.name());
		}
		return eventDBObj;
	}

	private static ObjectMapper mapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
		}
		return mapper;
	}
}
