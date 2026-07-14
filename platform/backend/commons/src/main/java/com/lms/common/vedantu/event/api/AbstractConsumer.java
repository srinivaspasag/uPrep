package com.lms.common.vedantu.event.api;

import com.lms.common.vedantu.mongo.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public abstract class AbstractConsumer implements IConsumer {

	private static final Logger logger = LoggerFactory.getLogger(AbstractConsumer.class);

	protected void preProcess(IConsumable consumable) {
		// do nothing
	}

	protected abstract void process(IConsumable consumable);

	protected void postProcess(IConsumable consumable) {
		// do nothing
	}

	@Override
	public void consume(IConsumable consumable) {
		preProcess(consumable);
		process(consumable);
		postProcess(consumable);
	}

	protected void remove(Event event, String collectionName) {
		/*MorphiaManager.INSTANCE.getDS().getDB().getCollection(collectionName);
		DB db = MorphiaManager.INSTANCE.getDS().getDB();
		db.requestStart();
		logInfo(event, "removing event : " + event._getStringId() + " form " + collectionName);
		DBCollection coll = db.getCollection(collectionName);
		DBObject rmEvent = new BasicDBObject();
		rmEvent.put("_id", event.id);
		coll.remove(rmEvent);
		db.requestDone();*/
	}

	protected void logWarn(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId() : "";
		logger.warn("[" + id + "]" + message);
	}

	protected void logInfo(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId() : "";
		logger.info("[" + id + "]" + message);
	}

	protected void logDebug(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId() : "";
		logger.debug("[" + id + "]" + message);
	}

	protected void logError(IConsumable consumable, String message, Throwable t) {
		String id = (null != consumable) ? consumable._getConsumableId() : "";
		logger.error("[" + id + "]" + message, t);
	}
}
